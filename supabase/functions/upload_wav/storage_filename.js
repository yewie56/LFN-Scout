export function ratingSuffix(rating) {
  return Number.isInteger(rating) && rating >= 0 && rating <= 5
    ? `R${rating}`
    : "RX";
}

export function normalizeSourceCode(value) {
  return value === "MIC" ? "MIC" : "UNP";
}

export function timestampForFilename(recordedAt) {
  const match =
    /^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2}):(\d{2})/.exec(recordedAt);

  if (!match) {
    throw new Error("invalid_recorded_at");
  }

  return `${match[1]}_${match[2]}-${match[3]}-${match[4]}`;
}

export function buildStoragePath({
  observerId,
  recordedAt,
  gainDb,
  sourceCode,
  rating,
}) {
  const safeObserverId = String(observerId).trim();

  if (!/^[A-Za-z0-9_-]+$/.test(safeObserverId)) {
    throw new Error("invalid_observer_id");
  }

  if (!Number.isInteger(gainDb) || gainDb < 0 || gainDb > 50) {
    throw new Error("invalid_gain");
  }

  const timestamp = timestampForFilename(recordedAt);
  const source = normalizeSourceCode(sourceCode);
  const ratingPart = ratingSuffix(rating);

  return (
    `${safeObserverId}/` +
    `${safeObserverId}_${timestamp}_G${gainDb}_${source}_${ratingPart}.wav`
  );
}
