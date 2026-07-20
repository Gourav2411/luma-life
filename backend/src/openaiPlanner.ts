import OpenAI from "openai";
import { z } from "zod";
import {
  interpretedTurnSchema,
  type AssistantTurnRequest,
  type InterpretedTurn,
  type PlanItem,
  type PlanProposal,
} from "./contracts.js";
import type { ModelRegistry } from "./modelRegistry.js";

const explanationSchema = z.object({
  assistant_message: z.string().min(1).max(2_000),
  clarification: z.string().min(1).max(500).nullable(),
});

type PlanningContext = {
  locale: string;
  timezone: string;
  sleep_window: { start_minute: number; end_minute: number };
  existing_items: PlanItem[];
  confirmed_memories: Array<{ category: string; statement: string }>;
};

export class OpenAIPlanningPipeline {
  constructor(
    private readonly client: OpenAI,
    private readonly models: ModelRegistry,
  ) {}

  async interpret(
    request: AssistantTurnRequest,
    context: PlanningContext,
  ): Promise<InterpretedTurn> {
    const contextCall = await this.client.responses.create({
      model: this.models.extraction,
      store: false,
      instructions:
        "You interpret student planning requests. First call get_planning_context. Preserve dates, names and times exactly, including Hinglish. Never invent capacity or calendar facts.",
      input: request.input.text,
      tools: [
        {
          type: "function",
          name: "get_planning_context",
          description: "Load the authenticated student's current constraints and confirmed planning memories.",
          strict: true,
          parameters: {
            type: "object",
            properties: {
              horizon: { type: "string", enum: ["direction", "semester", "week", "day", "session"] },
              client_context_version: { type: "integer", minimum: 0 },
            },
            required: ["horizon", "client_context_version"],
            additionalProperties: false,
          },
        },
      ],
      tool_choice: { type: "function", name: "get_planning_context" },
    });
    const functionCall = contextCall.output.find((item) => item.type === "function_call");
    if (!functionCall || functionCall.type !== "function_call") {
      throw new Error("Model did not request authenticated planning context.");
    }

    const interpreted = await this.client.responses.create({
      model: this.models.extraction,
      store: false,
      previous_response_id: contextCall.id,
      instructions:
        "Extract intent, constraints, sentiment, uncertainty, work requests and candidate memories. A candidate is not confirmed memory. Return only the strict schema.",
      input: [
        {
          type: "function_call_output",
          call_id: functionCall.call_id,
          output: JSON.stringify(context),
        },
      ],
      text: {
        format: {
          type: "json_schema",
          name: "luma_interpreted_turn",
          strict: true,
          schema: z.toJSONSchema(interpretedTurnSchema),
        },
      },
    });
    return interpretedTurnSchema.parse(JSON.parse(interpreted.output_text));
  }

  async explain(
    request: AssistantTurnRequest,
    interpreted: InterpretedTurn,
    proposal: PlanProposal,
  ): Promise<z.infer<typeof explanationSchema>> {
    const response = await this.client.responses.create({
      model: this.models.planning,
      store: false,
      instructions: [
        "Explain a deterministic, already-validated plan proposal.",
        "Do not add, remove or alter changes.",
        "State why the proposal fits, what assumptions were made, and which protected plans stayed intact.",
        "Ask a clarification only when unresolved_questions is non-empty.",
        "Use supportive language without diagnosis, guilt, streak pressure or productivity shaming.",
      ].join(" "),
      input: JSON.stringify({
        student_input: request.input.text,
        interpreted,
        validated_proposal: proposal,
      }),
      text: {
        format: {
          type: "json_schema",
          name: "luma_proposal_explanation",
          strict: true,
          schema: z.toJSONSchema(explanationSchema),
        },
      },
    });
    return explanationSchema.parse(JSON.parse(response.output_text));
  }
}

export type { PlanningContext };
