/**
 * Design boundary: Ateliê de Inteligência keeps operational state explicit and
 * calm; run ordering remains independent from presentation components.
 */
import type { ExecutionRunStatusResponse } from "../../api/client";

export function mergeRuns(
  current: ExecutionRunStatusResponse[],
  incoming: ExecutionRunStatusResponse[],
): ExecutionRunStatusResponse[] {
  const byId = new Map(current.map((run) => [run.runId, run]));
  incoming.forEach((run) => byId.set(run.runId, run));
  return Array.from(byId.values()).sort(
    (left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt),
  );
}

export function newestUpdatedAt(
  runs: ExecutionRunStatusResponse[],
  current?: string,
): string | undefined {
  return runs.reduce<string | undefined>((latest, run) => {
    if (!latest || Date.parse(run.updatedAt) > Date.parse(latest)) return run.updatedAt;
    return latest;
  }, current);
}
