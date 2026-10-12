import assert from "node:assert/strict";
import {
  buildStoragePath,
  ratingSuffix,
} from "./storage_filename.js";

assert.equal(ratingSuffix(0), "R0");
assert.equal(ratingSuffix(5), "R5");
assert.equal(ratingSuffix(null), "RX");

assert.equal(
  buildStoragePath({
    observerId: "BB001",
    recordedAt: "2026-09-09T01:23:45.987+02:00",
    gainDb: 30,
    sourceCode: "UNP",
    rating: 3,
  }),
  "BB001/BB001_2026-09-09_01-23-45_G30_UNP_R3.wav"
);

assert.equal(
  buildStoragePath({
    observerId: "BB001",
    recordedAt: "2026-09-09T01:23:45+02:00",
    gainDb: 30,
    sourceCode: "MIC",
    rating: null,
  }),
  "BB001/BB001_2026-09-09_01-23-45_G30_MIC_RX.wav"
);

console.log("storage filename tests PASS");
