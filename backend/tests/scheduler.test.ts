import { describe, expect, it } from "vitest";
import type { InterpretedTurn, PlanItem } from "../src/contracts.js";
import { DeterministicScheduler } from "../src/scheduler.js";

const day = "2026-07-20";
const fixed = (
  id: string,
  title: string,
  start: number,
  duration: number,
  flexibility: "fixed" | "protected" = "fixed",
): PlanItem => ({
  id,
  title,
  date: day,
  start_minute: start,
  duration_minutes: duration,
  life_area: flexibility === "protected" ? "relationships" : "academics",
  flexibility,
  source: "calendar",
  state: "active",
  energy: "steady",
  parent_goal_id: null,
  travel_before_minutes: 0,
  travel_after_minutes: 0,
});

const interpreted = (overrides: Partial<InterpretedTurn> = {}): InterpretedTurn => ({
  intent: "plan",
  goal: "Prepare for exam",
  horizon: "day",
  sentiment: "tired",
  energy: "low",
  constraints: [],
  work_requests: [
    {
      title: "Exam review",
      date: day,
      duration_minutes: 45,
      life_area: "academics",
      preferred_start_minute: 16 * 60,
      earliest_start_minute: 8 * 60,
      latest_end_minute: 22 * 60,
      energy: "low",
    },
  ],
  uncertainty: [],
  memory_candidates: [],
  ...overrides,
});

describe("DeterministicScheduler", () => {
  it("protects football and date night while finding a feasible exam slot", () => {
    const existing = [
      fixed("00000000-0000-4000-8000-000000000001", "Class", 9 * 60, 60),
      fixed("00000000-0000-4000-8000-000000000002", "Football", 18 * 60, 75, "protected"),
      fixed("00000000-0000-4000-8000-000000000003", "Date night", 20 * 60 + 30, 90, "protected"),
    ];
    const proposal = new DeterministicScheduler().createProposal(existing, interpreted());
    expect(proposal.is_feasible).toBe(true);
    expect(proposal.changes).toHaveLength(1);
    expect(proposal.changes[0]?.after?.start_minute).not.toBe(18 * 60);
    expect(existing.every((item) => item.state === "active")).toBe(true);
  });

  it("returns a trade-off instead of inventing capacity", () => {
    const existing = [
      fixed("00000000-0000-4000-8000-000000000011", "All-day lab", 6 * 60, 16 * 60 + 30),
    ];
    const proposal = new DeterministicScheduler().createProposal(existing, interpreted());
    expect(proposal.is_feasible).toBe(false);
    expect(proposal.unresolved_questions[0]).toContain("No feasible");
  });

  it("detects travel-buffer overlaps", () => {
    const first = { ...fixed("00000000-0000-4000-8000-000000000021", "Class", 9 * 60, 60), travel_after_minutes: 30 };
    const second = fixed("00000000-0000-4000-8000-000000000022", "Interview", 10 * 60 + 15, 45);
    expect(new DeterministicScheduler().validate([first, second])[0]).toContain("travel buffers");
  });
});
