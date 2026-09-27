package com.kasane.assistant;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Signs assistant reply text so a later request can prove a given "assistant" history
 * turn actually came from this server, rather than being fabricated by the client to
 * prime the model (e.g. pretending the assistant already agreed to ignore its scope).
 * The chat API is deliberately stateless - no server-side conversation storage - so
 * this is how integrity is achieved without persisting conversations: the proof travels
 * with the turn instead of the turn living in server memory.
 *
 * The signing key is generated fresh per process start, not persisted anywhere - a
 * backend restart invalidates every previously-signed turn, so an in-flight
 * conversation's history fails verification and the client has to start a new one.
 * That's an acceptable tradeoff at this app's scale (matches the rest of the app's
 * ephemeral-dev philosophy - e.g. the dev DB wipes on every restart too).
 */
@Service
public class HistoryIntegrityService {

    private static final String ALGORITHM = "HmacSHA256";
    private final SecretKeySpec secretKey;

    public HistoryIntegrityService() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String sign(String content) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            byte[] signed = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign content", e);
        }
    }

    public boolean verify(String content, String signature) {
        if (signature == null) {
            return false;
        }
        String expected = sign(content);
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            signature.getBytes(StandardCharsets.UTF_8));
    }
}
