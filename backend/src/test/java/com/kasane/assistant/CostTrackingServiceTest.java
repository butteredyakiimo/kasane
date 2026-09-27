package com.kasane.assistant;

import com.anthropic.models.messages.CacheCreation;
import com.anthropic.models.messages.Usage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CostTrackingServiceTest {

    private final CostTrackingService service = new CostTrackingService();

    private static Usage usage(long inputTokens, long outputTokens, long cacheReadTokens, long cacheWriteTokens) {
        return Usage.builder()
            .inputTokens(inputTokens)
            .outputTokens(outputTokens)
            .cacheReadInputTokens(cacheReadTokens)
            .cacheCreationInputTokens(cacheWriteTokens)
            .cacheCreation(CacheCreation.builder()
                .ephemeral5mInputTokens(cacheWriteTokens)
                .ephemeral1hInputTokens(0)
                .build())
            .inferenceGeo(java.util.Optional.empty())
            .serverToolUse(java.util.Optional.empty())
            .serviceTier(java.util.Optional.empty())
            .build();
    }

    @Test
    void estimateCost_sonnet_noCache() {
        Usage usage = usage(1_000_000, 1_000_000, 0, 0);

        Double cost = service.estimateCost("claude-sonnet-5", usage);

        // $2/MTok input + $10/MTok output at exactly 1M tokens each
        assertThat(cost).isCloseTo(12.00, within(0.0001));
    }

    @Test
    void estimateCost_haiku_noCache() {
        Usage usage = usage(1_000_000, 1_000_000, 0, 0);

        Double cost = service.estimateCost("claude-haiku-4-5", usage);

        // $1/MTok input + $5/MTok output at exactly 1M tokens each
        assertThat(cost).isCloseTo(6.00, within(0.0001));
    }

    @Test
    void estimateCost_includesCacheReadAndWriteAtDiscountedRate() {
        Usage usage = usage(0, 0, 1_000_000, 1_000_000);

        Double cost = service.estimateCost("claude-sonnet-5", usage);

        // cache read = 0.1x input rate ($0.20), cache write = 1.25x input rate ($2.50)
        assertThat(cost).isCloseTo(0.20 + 2.50, within(0.0001));
    }

    @Test
    void estimateCost_smallRealisticCall_isFractionsOfACent() {
        Usage usage = usage(1000, 200, 0, 0);

        Double cost = service.estimateCost("claude-sonnet-5", usage);

        assertThat(cost).isCloseTo(0.004, within(0.0001));
    }

    @Test
    void estimateCost_returnsNull_forUnknownModel() {
        Usage usage = usage(100, 100, 0, 0);

        assertThat(service.estimateCost("claude-opus-5", usage)).isNull();
    }

    @Test
    void log_accumulatesRunningTotalAcrossCalls() {
        Usage usage = usage(1_000_000, 0, 0, 0);

        service.log("claude-sonnet-5", usage); // $2.00
        service.log("claude-sonnet-5", usage); // +$2.00

        assertThat(service.sessionTotalUsd()).isCloseTo(4.00, within(0.0001));
    }

    @Test
    void log_doesNotThrow_andLeavesTotalUnchanged_forUnknownModel() {
        Usage usage = usage(100, 100, 0, 0);

        service.log("claude-opus-5", usage);

        assertThat(service.sessionTotalUsd()).isZero();
    }
}
