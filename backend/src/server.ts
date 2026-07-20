import { randomUUID } from "node:crypto";
import cors from "cors";
import express, { type NextFunction, type Request, type Response } from "express";
import OpenAI from "openai";
import { createClient } from "@supabase/supabase-js";
import { assistantTurnRequestSchema, horizonSchema } from "./contracts.js";
import { modelRegistry } from "./modelRegistry.js";
import { OpenAIPlanningPipeline } from "./openaiPlanner.js";
import { ProposalService } from "./proposalService.js";
import { DeterministicScheduler } from "./scheduler.js";
import { routeSafety } from "./safety.js";

const required = (name: string): string => {
  const value = process.env[name];
  if (!value) throw new Error(`Missing required server environment variable: ${name}`);
  return value;
};

const db = createClient(required("SUPABASE_URL"), required("SUPABASE_SERVICE_ROLE_KEY"), {
  auth: { persistSession: false, autoRefreshToken: false },
});
const openai = new OpenAI({ apiKey: required("OPENAI_API_KEY") });
const pipeline = new OpenAIPlanningPipeline(openai, modelRegistry());
const scheduler = new DeterministicScheduler();
const proposals = new ProposalService(db);
const app = express();
app.use(cors({ origin: false }));
app.use(express.json({ limit: "64kb" }));

type AuthenticatedRequest = Request & { userId?: string };

async function authenticate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  const token = req.headers.authorization?.replace(/^Bearer\s+/i, "");
  if (!token) return res.status(401).json({ error: "missing_bearer_token" });
  const { data, error } = await db.auth.getUser(token);
  if (error || !data.user) return res.status(401).json({ error: "invalid_session" });
  req.userId = data.user.id;
  next();
}

app.get("/health", (_req, res) => {
  res.json({ ok: true, service: "luma-planning", models: modelRegistry() });
});

app.post("/v1/assistant/turn", authenticate, async (req: AuthenticatedRequest, res, next) => {
  try {
    const request = assistantTurnRequestSchema.parse(req.body);
    const safety = routeSafety(request.input.text);
    if (safety.route === "immediate_support") {
      return res.json({
        assistant_message: safety.message,
        clarification: null,
        memory_candidates: [],
        proposal: null,
        safety_route: safety.route,
        trace_id: randomUUID(),
      });
    }
    const userId = req.userId!;
    const anchorDate = new Date().toISOString().slice(0, 10);
    const context = await proposals.getDayContext(userId, anchorDate);
    const { data: memories, error: memoryError } = await db
      .from("memory_facts")
      .select("category, statement")
      .eq("user_id", userId)
      .eq("is_confirmed", true);
    if (memoryError) throw memoryError;

    const interpreted = await pipeline.interpret(request, {
      locale: context.profile.locale,
      timezone: context.profile.timezone,
      sleep_window: {
        start_minute: context.profile.sleep_start_minute,
        end_minute: context.profile.sleep_end_minute,
      },
      existing_items: context.items,
      confirmed_memories: memories ?? [],
    });
    const proposal = scheduler.createProposal(context.items, interpreted);
    await proposals.saveDraft(userId, request.conversation_id, proposal);
    const explanation = await pipeline.explain(request, interpreted, proposal);
    return res.json({
      ...explanation,
      memory_candidates: interpreted.memory_candidates,
      proposal,
      safety_route: "planning",
      trace_id: randomUUID(),
    });
  } catch (error) {
    next(error);
  }
});

app.post(
  "/v1/plan-proposals/:id/decision",
  authenticate,
  async (req: AuthenticatedRequest, res, next) => {
    try {
      const proposalId = req.params.id;
      if (typeof proposalId !== "string") throw new Error("invalid_proposal_id");
      res.json(await proposals.decide(req.userId!, proposalId, req.body));
    } catch (error) {
      next(error);
    }
  },
);

app.get("/v1/plans", authenticate, async (req: AuthenticatedRequest, res, next) => {
  try {
    const horizon = horizonSchema.parse(req.query.horizon);
    const anchor = zodDate(req.query.anchor);
    const { data, error } = await db.rpc("get_plan_hierarchy", {
      p_user_id: req.userId!,
      p_horizon: horizon,
      p_anchor: anchor,
    });
    if (error) throw error;
    res.json(data);
  } catch (error) {
    next(error);
  }
});

app.use((error: unknown, _req: Request, res: Response, _next: NextFunction) => {
  const traceId = randomUUID();
  console.error(JSON.stringify({ traceId, error }));
  res.status(400).json({ error: "request_failed", trace_id: traceId });
});

const port = Number(process.env.PORT ?? 8787);
app.listen(port, () => console.log(`Luma planning service listening on :${port}`));

function zodDate(value: unknown): string {
  if (typeof value !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    throw new Error("anchor must be an ISO date");
  }
  return value;
}
