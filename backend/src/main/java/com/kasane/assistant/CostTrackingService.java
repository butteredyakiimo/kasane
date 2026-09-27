package com.kasane.assistant;

import com.anthropic.models.messages.Usage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Logs per-call cost for the assistant's Claude API calls, plus a running total since
 * process start. In-memory only - a restart resets the total - this is for visibility
 * during development/early usage, not a billing system of record (use the Anthropic
 * Console's usage/cost reporting for that).
 */
@Service
@Slf4j
public class CostTrackingService {

    private record Pricing(double inputPerMTok, double outputPerMTok) {}

    // $/million tokens, first-party API rates.
    private static final Map<String, Pricing> PRICING = Map.of(
        "claude-haiku-4-5", new Pricing(1.00, 5.00),
        "claude-sonnet-5", new Pricing(2.00, 10.00)
    );

    private static final double CACHE_READ_MULTIPLIER = 0.1;
    private static final double CACHE_WRITE_MULTIPLIER = 1.25;

    private final AtomicReference<Double> totalCostUsd = new AtomicReference<>(0.0);

    public void log(String model, Usage usage) {
        Double cost = estimateCost(model, usage);
        if (cost == null) {
            log.warn("No pricing entry for model {}, skipping cost log", model);
            return;
        }

        double runningTotal = totalCostUsd.updateAndGet(prev -> prev + cost);

        log.info("assistant call: model={} inputTokens={} outputTokens={} cost=${} sessionTotal=${}",
            model, usage.inputTokens(), usage.outputTokens(),
            String.format("%.6f", cost), String.format("%.4f", runningTotal));
    }

    double sessionTotalUsd() {
        return totalCostUsd.get();
    }

    /** Returns the estimated USD cost for one call, or null if the model has no pricing entry. */
    Double estimateCost(String model, Usage usage) {
        Pricing pricing = PRICING.get(model);
        if (pricing == null) {
            return null;
        }

        long inputTokens = usage.inputTokens();
        long outputTokens = usage.outputTokens();
        long cacheReadTokens = usage.cacheReadInputTokens().orElse(0L);
        long cacheWriteTokens = usage.cacheCreationInputTokens().orElse(0L);

        return inputTokens / 1_000_000.0 * pricing.inputPerMTok()
            + outputTokens / 1_000_000.0 * pricing.outputPerMTok()
            + cacheReadTokens / 1_000_000.0 * pricing.inputPerMTok() * CACHE_READ_MULTIPLIER
            + cacheWriteTokens / 1_000_000.0 * pricing.inputPerMTok() * CACHE_WRITE_MULTIPLIER;
    }
}
