import { randomUUID } from "node:crypto";
import type { InterpretedTurn, PlanChange, PlanItem, PlanProposal } from "./contracts.js";

export type SchedulerPreferences = {
  dayStartMinute: number;
  dayEndMinute: number;
  focusPeakStartMinute: number;
  focusPeakEndMinute: number;
  minimumBufferMinutes: number;
};

const defaults: SchedulerPreferences = {
  dayStartMinute: 6 * 60,
  dayEndMinute: 22 * 60 + 30,
  focusPeakStartMinute: 9 * 60,
  focusPeakEndMinute: 13 * 60,
  minimumBufferMinutes: 10,
};

export class DeterministicScheduler {
  constructor(private readonly preferences: SchedulerPreferences = defaults) {}

  createProposal(existing: PlanItem[], interpreted: InterpretedTurn): PlanProposal {
    const currentValidation = this.validate(existing);
    if (currentValidation.length > 0) {
      return this.impossible(
        interpreted.horizon,
        "The current plan already contains a hard conflict.",
        currentValidation,
      );
    }

    const working = [...existing];
    const changes: PlanChange[] = [];
    const unresolved: string[] = [...interpreted.uncertainty];
    const requests = [...interpreted.work_requests].sort(
      (left, right) =>
        Number(right.life_area === "academics") - Number(left.life_area === "academics") ||
        right.duration_minutes - left.duration_minutes,
    );

    for (const request of requests) {
      const slot = this.bestSlot(working, request);
      if (slot === null) {
        unresolved.push(
          `No feasible ${request.duration_minutes}-minute slot was found for ${request.title}.`,
        );
        continue;
      }
      const created: PlanItem = {
        id: randomUUID(),
        title: request.title,
        date: request.date,
        start_minute: slot,
        duration_minutes: request.duration_minutes,
        life_area: request.life_area,
        flexibility: "flexible",
        source: "luma",
        state: "draft",
        energy: request.energy,
        parent_goal_id: null,
        travel_before_minutes: 0,
        travel_after_minutes: 0,
      };
      working.push(created);
      changes.push({
        id: randomUUID(),
        action: "create",
        target_id: null,
        before: null,
        after: created,
        reason:
          request.preferred_start_minute === slot
            ? "Matches the preferred time without moving fixed or protected plans."
            : "Uses the highest-scoring conflict-free slot while preserving buffers.",
        affected_life_areas: [request.life_area],
        risk: "low",
      });
    }

    const validationMessages = this.validate(working);
    const isFeasible = unresolved.length === 0 && validationMessages.length === 0;
    return {
      id: randomUUID(),
      horizon: interpreted.horizon,
      summary: isFeasible
        ? "A feasible plan that protects commitments and leaves recovery space."
        : "The full request does not fit safely yet. Choose a trade-off.",
      assumptions: interpreted.constraints
        .filter((constraint) => constraint.certainty < 1)
        .map((constraint) => `${constraint.label} interpreted with ${Math.round(constraint.certainty * 100)}% certainty.`),
      changes,
      unresolved_questions: unresolved,
      confidence: isFeasible ? 0.92 : 0.55,
      is_feasible: isFeasible,
      validation_messages: validationMessages,
    };
  }

  validate(items: PlanItem[]): string[] {
    const messages: string[] = [];
    const scheduled = items
      .filter((item) => item.start_minute !== null && item.state !== "skipped")
      .sort((left, right) => (left.start_minute ?? 0) - (right.start_minute ?? 0));

    for (const item of scheduled) {
      const start = item.start_minute;
      if (start === null) continue;
      const end = start + item.duration_minutes;
      if (start < this.preferences.dayStartMinute || end > this.preferences.dayEndMinute) {
        messages.push(`${item.title} falls outside the confirmed waking window.`);
      }
    }
    for (let index = 0; index < scheduled.length - 1; index += 1) {
      const first = scheduled[index];
      const second = scheduled[index + 1];
      if (!first || !second || first.date !== second.date) continue;
      const firstStart = first.start_minute;
      const secondStart = second.start_minute;
      if (firstStart === null || secondStart === null) continue;
      const blockedEnd =
        firstStart + first.duration_minutes + first.travel_after_minutes + second.travel_before_minutes;
      if (blockedEnd > secondStart) {
        messages.push(`${first.title} overlaps ${second.title} including travel buffers.`);
      }
    }
    return messages;
  }

  private bestSlot(
    existing: PlanItem[],
    request: InterpretedTurn["work_requests"][number],
  ): number | null {
    const earliest = Math.max(request.earliest_start_minute, this.preferences.dayStartMinute);
    const latest =
      Math.min(request.latest_end_minute, this.preferences.dayEndMinute) - request.duration_minutes;
    let best: { start: number; score: number } | null = null;
    for (let start = earliest; start <= latest; start += 15) {
      if (!this.canPlace(existing, request.date, start, request.duration_minutes)) continue;
      const preferredPenalty =
        request.preferred_start_minute === null ? 0 : Math.abs(start - request.preferred_start_minute);
      const peakPenalty =
        request.energy === "high" &&
        (start < this.preferences.focusPeakStartMinute || start > this.preferences.focusPeakEndMinute)
          ? 90
          : 0;
      const lowEnergyPenalty =
        (request.energy === "low" || request.energy === "overwhelmed") && start < 17 * 60 ? 30 : 0;
      const latePenalty = start >= 20 * 60 ? 120 : 0;
      const score = preferredPenalty + peakPenalty + lowEnergyPenalty + latePenalty;
      if (best === null || score < best.score) best = { start, score };
    }
    return best?.start ?? null;
  }

  private canPlace(existing: PlanItem[], date: string, start: number, duration: number): boolean {
    const end = start + duration;
    return existing.every((item) => {
      if (item.date !== date || item.start_minute === null || item.state === "skipped") return true;
      const blockedStart =
        item.start_minute - item.travel_before_minutes - this.preferences.minimumBufferMinutes;
      const blockedEnd =
        item.start_minute +
        item.duration_minutes +
        item.travel_after_minutes +
        this.preferences.minimumBufferMinutes;
      return end <= blockedStart || start >= blockedEnd;
    });
  }

  private impossible(horizon: InterpretedTurn["horizon"], summary: string, messages: string[]): PlanProposal {
    return {
      id: randomUUID(),
      horizon,
      summary,
      assumptions: [],
      changes: [],
      unresolved_questions: messages,
      confidence: 0,
      is_feasible: false,
      validation_messages: messages,
    };
  }
}
