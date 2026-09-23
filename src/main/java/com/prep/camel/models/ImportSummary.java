package com.prep.camel.models;

import java.time.Instant;

public record ImportSummary(int rejectedCount, int processedCount, String filename, Instant timestamp) {
}
