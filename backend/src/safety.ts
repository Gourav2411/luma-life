const immediateRiskPatterns = [
  /\bkill myself\b/i,
  /\bend my life\b/i,
  /\bsuicide\b/i,
  /\bhurt myself\b/i,
  /\bnot safe right now\b/i,
];

export type SafetyRoute =
  | { route: "planning" }
  | { route: "immediate_support"; message: string };

export function routeSafety(text: string): SafetyRoute {
  if (!immediateRiskPatterns.some((pattern) => pattern.test(text))) return { route: "planning" };
  return {
    route: "immediate_support",
    message:
      "I’m really sorry you’re carrying this. Planning can wait. Please contact someone you trust nearby or local emergency support now, and move away from anything you could use to hurt yourself. Luma does not diagnose or handle emergencies.",
  };
}
