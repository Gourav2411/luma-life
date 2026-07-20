export type ModelPurpose = "planning" | "extraction" | "realtime";

export type ModelRegistry = Readonly<Record<ModelPurpose, string>>;

export function modelRegistry(env: NodeJS.ProcessEnv = process.env): ModelRegistry {
  return {
    planning: env.LUMA_PLANNER_MODEL ?? "gpt-5.6-terra",
    extraction: env.LUMA_EXTRACTOR_MODEL ?? "gpt-5.6-luna",
    realtime: env.LUMA_REALTIME_MODEL ?? "",
  };
}
