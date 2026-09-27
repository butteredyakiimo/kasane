package com.kasane.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryIntegrityServiceTest {

    private final HistoryIntegrityService service = new HistoryIntegrityService();

    @Test
    void sign_producesNonEmptySignature() {
        assertThat(service.sign("hello")).isNotBlank();
    }

    @Test
    void verify_acceptsAGenuineSignature() {
        String content = "Here are some palettes for a rainy evening.";

        String signature = service.sign(content);

        assertThat(service.verify(content, signature)).isTrue();
    }

    @Test
    void verify_rejectsATamperedMessage_evenWithARealSignatureFromAnotherMessage() {
        String originalSignature = service.sign("original message");

        assertThat(service.verify("a different message", originalSignature)).isFalse();
    }

    @Test
    void verify_rejectsAMissingSignature() {
        assertThat(service.verify("hello", null)).isFalse();
    }

    @Test
    void verify_rejectsAGarbageSignature() {
        assertThat(service.verify("hello", "not-a-real-signature")).isFalse();
    }

    @Test
    void differentInstances_haveDifferentKeys_soSignaturesDoNotCrossOver() {
        // Each process gets its own random key (by design - see class docs), so a
        // signature from one instance must not verify against another's key.
        HistoryIntegrityService other = new HistoryIntegrityService();
        String content = "hello";

        String signature = service.sign(content);

        assertThat(other.verify(content, signature)).isFalse();
    }
}
