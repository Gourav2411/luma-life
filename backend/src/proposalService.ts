import type { SupabaseClient } from "@supabase/supabase-js";
import {
  decisionRequestSchema,
  planItemSchema,
  planProposalSchema,
  type PlanProposal,
} from "./contracts.js";

export class ProposalService {
  constructor(private readonly db: SupabaseClient) {}

  async saveDraft(userId: string, conversationId: string, proposal: PlanProposal): Promise<void> {
    const validated = planProposalSchema.parse(proposal);
    const { error } = await this.db.from("plan_proposals").insert({
      id: validated.id,
      user_id: userId,
      conversation_id: conversationId,
      horizon: validated.horizon,
      summary: validated.summary,
      assumptions: validated.assumptions,
      unresolved_questions: validated.unresolved_questions,
      confidence: validated.confidence,
      is_feasible: validated.is_feasible,
      validation_messages: validated.validation_messages,
      state: "draft",
      proposal_json: validated,
    });
    if (error) throw error;
  }

  async decide(
    userId: string,
    proposalId: string,
    input: unknown,
  ): Promise<{ plan: unknown; sync: { state: "not_required" | "queued"; outbox_ids: string[] } }> {
    const decision = decisionRequestSchema.parse(input);
    const acceptedIds =
      decision.decision === "approve"
        ? null
        : decision.decision === "partial"
          ? decision.accepted_change_ids
          : [];
    const { data, error } = await this.db.rpc("apply_plan_proposal", {
      p_user_id: userId,
      p_proposal_id: proposalId,
      p_accepted_change_ids: acceptedIds,
      p_expected_sync_version: decision.expected_sync_version,
      p_decision: decision.decision,
    });
    if (error) throw error;
    return data as { plan: unknown; sync: { state: "not_required" | "queued"; outbox_ids: string[] } };
  }

  async getDayContext(userId: string, anchorDate: string) {
    const [{ data: profile, error: profileError }, { data: items, error: itemsError }] =
      await Promise.all([
        this.db
          .from("profiles")
          .select("locale, timezone, sleep_start_minute, sleep_end_minute")
          .eq("id", userId)
          .single(),
        this.db
          .from("plan_items")
          .select("*")
          .eq("user_id", userId)
          .eq("date", anchorDate)
          .neq("state", "archived")
          .order("start_minute"),
      ]);
    if (profileError) throw profileError;
    if (itemsError) throw itemsError;
    return {
      profile,
      items: (items ?? []).map((item) => planItemSchema.parse(item)),
    };
  }
}
