import { z } from "zod";

export const lifeAreaSchema = z.enum([
  "academics",
  "skills",
  "health",
  "relationships",
  "fun",
  "self_direction",
]);
export type LifeArea = z.infer<typeof lifeAreaSchema>;

export const horizonSchema = z.enum(["direction", "semester", "week", "day", "session"]);
export type Horizon = z.infer<typeof horizonSchema>;

export const planItemSchema = z.object({
  id: z.string().uuid(),
  title: z.string().min(1).max(240),
  date: z.iso.date(),
  start_minute: z.number().int().min(0).max(1439).nullable(),
  duration_minutes: z.number().int().positive().max(720),
  life_area: lifeAreaSchema,
  flexibility: z.enum(["fixed", "flexible", "protected"]),
  source: z.enum(["user", "calendar", "luma"]),
  state: z.enum(["draft", "active", "completed", "skipped", "rescheduled"]),
  energy: z.enum(["low", "steady", "high", "overwhelmed"]),
  parent_goal_id: z.string().uuid().nullable().default(null),
  travel_before_minutes: z.number().int().min(0).max(240).default(0),
  travel_after_minutes: z.number().int().min(0).max(240).default(0),
});
export type PlanItem = z.infer<typeof planItemSchema>;

export const assistantTurnRequestSchema = z.object({
  conversation_id: z.string().uuid(),
  input: z.object({
    type: z.enum(["text", "transcript"]),
    text: z.string().min(1).max(12_000),
  }),
  horizon: horizonSchema,
  client_context_version: z.number().int().nonnegative(),
});
export type AssistantTurnRequest = z.infer<typeof assistantTurnRequestSchema>;

export const planChangeSchema = z.object({
  id: z.string().uuid(),
  action: z.enum(["create", "move", "resize", "delete", "protect"]),
  target_id: z.string().uuid().nullable(),
  before: planItemSchema.nullable(),
  after: planItemSchema.nullable(),
  reason: z.string().min(1),
  affected_life_areas: z.array(lifeAreaSchema).min(1),
  risk: z.enum(["low", "medium", "high"]),
});
export type PlanChange = z.infer<typeof planChangeSchema>;

export const planProposalSchema = z.object({
  id: z.string().uuid(),
  horizon: horizonSchema,
  summary: z.string().min(1),
  assumptions: z.array(z.string()),
  changes: z.array(planChangeSchema),
  unresolved_questions: z.array(z.string()),
  confidence: z.number().min(0).max(1),
  is_feasible: z.boolean(),
  validation_messages: z.array(z.string()),
});
export type PlanProposal = z.infer<typeof planProposalSchema>;

export const decisionRequestSchema = z.object({
  decision: z.enum(["approve", "reject", "partial"]),
  accepted_change_ids: z.array(z.string().uuid()).default([]),
  expected_sync_version: z.number().int().nonnegative(),
});

export const interpretedTurnSchema = z.object({
  intent: z.enum(["plan", "capture", "reflect", "question", "support"]),
  goal: z.string().nullable(),
  horizon: horizonSchema,
  sentiment: z.enum(["neutral", "tired", "stressed", "positive", "overwhelmed"]),
  energy: z.enum(["low", "steady", "high", "overwhelmed"]),
  constraints: z.array(
    z.object({
      label: z.string(),
      date: z.iso.date().nullable(),
      start_minute: z.number().int().min(0).max(1439).nullable(),
      duration_minutes: z.number().int().positive().max(720).nullable(),
      life_area: lifeAreaSchema,
      flexibility: z.enum(["fixed", "flexible", "protected"]),
      certainty: z.number().min(0).max(1),
    }),
  ),
  work_requests: z.array(
    z.object({
      title: z.string(),
      date: z.iso.date(),
      duration_minutes: z.number().int().positive().max(360),
      life_area: lifeAreaSchema,
      preferred_start_minute: z.number().int().min(0).max(1439).nullable(),
      earliest_start_minute: z.number().int().min(0).max(1439),
      latest_end_minute: z.number().int().min(1).max(1440),
      energy: z.enum(["low", "steady", "high", "overwhelmed"]),
    }),
  ),
  uncertainty: z.array(z.string()),
  memory_candidates: z.array(
    z.object({
      category: z.enum(["planning_preference", "durable_fact", "sensitive_fact"]),
      statement: z.string(),
      requires_confirmation: z.boolean(),
    }),
  ),
});
export type InterpretedTurn = z.infer<typeof interpretedTurnSchema>;
