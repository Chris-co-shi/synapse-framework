package com.indigo.synapse.security.gatewayproof;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacSha256GatewayProofVerifierTest {

    private static final String GATEWAY_ID = "synapse-gateway";
    private static final String SECRET = "01234567890123456789012345678901";
    private static final long NOW = 1_700_000_000_000L;
    private static final Clock CLOCK = Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC);

    private final GatewayProofSigner signer = new HmacSha256GatewayProofSigner();
    private final GatewayProofTokenHasher tokenHasher = new GatewayProofTokenHasher();

    @Test
    void shouldVerifyValidProof() {
        HmacSha256GatewayProofVerifier verifier = verifier(null, false);
        SignedRequest signedRequest = signedRequest("nonce-1", NOW, "GET", "/api/items", "b=2&a=1", "token-a");

        GatewayProofVerificationResult result = verifier.verify(signedRequest.proof(), signedRequest.request());

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectMissingProof() {
        HmacSha256GatewayProofVerifier verifier = verifier(null, false);

        GatewayProofVerificationResult result = verifier.verify(null, null);

        assertEquals(GatewayProofVerificationStatus.MISSING, result.status());
    }

    @Test
    void shouldRejectUnknownGateway() {
        HmacSha256GatewayProofVerifier verifier = verifier(null, false);
        SignedRequest signedRequest = signedRequest("nonce-1", NOW, "GET", "/api/items", "", "token-a");
        GatewayProof proof = new GatewayProof(
                signedRequest.proof().version(),
                "unknown",
                signedRequest.proof().timestamp(),
                signedRequest.proof().nonce(),
                signedRequest.proof().signature()
        );

        GatewayProofVerificationResult result = verifier.verify(proof, signedRequest.request());

        assertEquals(GatewayProofVerificationStatus.UNKNOWN_GATEWAY, result.status());
    }

    @Test
    void shouldRejectExpiredTimestamp() {
        HmacSha256GatewayProofVerifier verifier = verifier(null, false);
        SignedRequest signedRequest = signedRequest("nonce-1", NOW - 61_000, "GET", "/api/items", "", "token-a");

        GatewayProofVerificationResult result = verifier.verify(signedRequest.proof(), signedRequest.request());

        assertEquals(GatewayProofVerificationStatus.EXPIRED, result.status());
    }

    @Test
    void shouldRejectInvalidSignatureWithoutWritingReplayStore() {
        InMemoryReplayStore replayStore = new InMemoryReplayStore();
        HmacSha256GatewayProofVerifier verifier = verifier(replayStore, true);
        SignedRequest signedRequest = signedRequest("nonce-1", NOW, "GET", "/api/items", "", "token-a");
        GatewayProof badProof = new GatewayProof(
                signedRequest.proof().version(),
                signedRequest.proof().gatewayId(),
                signedRequest.proof().timestamp(),
                signedRequest.proof().nonce(),
                signedRequest.proof().signature() + "x"
        );

        GatewayProofVerificationResult result = verifier.verify(badProof, signedRequest.request());

        assertEquals(GatewayProofVerificationStatus.INVALID_SIGNATURE, result.status());
        assertFalse(replayStore.contains(GATEWAY_ID, "nonce-1"));
    }

    @Test
    void shouldRejectReplayedNonceAfterSignatureSuccess() {
        InMemoryReplayStore replayStore = new InMemoryReplayStore();
        HmacSha256GatewayProofVerifier verifier = verifier(replayStore, true);
        SignedRequest signedRequest = signedRequest("nonce-1", NOW, "GET", "/api/items", "", "token-a");

        assertTrue(verifier.verify(signedRequest.proof(), signedRequest.request()).isSuccess());
        GatewayProofVerificationResult result = verifier.verify(signedRequest.proof(), signedRequest.request());

        assertEquals(GatewayProofVerificationStatus.REPLAYED, result.status());
        assertTrue(replayStore.lastTtl().toMillis() > 0);
    }

    @Test
    void shouldRejectInvalidNonce() {
        HmacSha256GatewayProofVerifier verifier = verifier(null, false);
        String nonce = "x".repeat(HmacSha256GatewayProofVerifier.MAX_NONCE_LENGTH + 1);
        SignedRequest signedRequest = signedRequest(nonce, NOW, "GET", "/api/items", "", "token-a");

        GatewayProofVerificationResult result = verifier.verify(signedRequest.proof(), signedRequest.request());

        assertEquals(GatewayProofVerificationStatus.INVALID_REQUEST, result.status());
    }

    @Test
    void shouldFailFastForInvalidConfigurationByDefault() {
        assertThrows(IllegalArgumentException.class, () -> new HmacSha256GatewayProofVerifier(
                Map.of(GATEWAY_ID, "short"),
                Duration.ofSeconds(60),
                CLOCK,
                null,
                false
        ));
    }

    @Test
    void shouldReturnConfigurationErrorWhenFailFastIsDisabled() {
        HmacSha256GatewayProofVerifier verifier = new HmacSha256GatewayProofVerifier(
                Map.of(GATEWAY_ID, "short"),
                Duration.ofSeconds(60),
                CLOCK,
                null,
                false,
                false
        );

        GatewayProofVerificationResult result = verifier.verify(null, null);

        assertEquals(GatewayProofVerificationStatus.CONFIGURATION_INVALID, result.status());
    }

    private HmacSha256GatewayProofVerifier verifier(GatewayProofReplayStore replayStore, boolean replayEnabled) {
        return new HmacSha256GatewayProofVerifier(
                Map.of(GATEWAY_ID, SECRET),
                Duration.ofSeconds(60),
                CLOCK,
                replayStore,
                replayEnabled
        );
    }

    private SignedRequest signedRequest(
            String nonce,
            long timestamp,
            String method,
            String path,
            String query,
            String token
    ) {
        GatewayProofCanonicalRequest request = new GatewayProofCanonicalRequest(
                GatewayProofVersion.V1,
                GATEWAY_ID,
                String.valueOf(timestamp),
                nonce,
                method,
                path,
                query,
                tokenHasher.sha256Hex(token)
        );
        String signature = signer.sign(request, SECRET);
        GatewayProof proof = new GatewayProof(GatewayProofVersion.V1, GATEWAY_ID, String.valueOf(timestamp), nonce, signature);
        return new SignedRequest(proof, request);
    }

    private record SignedRequest(GatewayProof proof, GatewayProofCanonicalRequest request) {
    }

    private static final class InMemoryReplayStore implements GatewayProofReplayStore {

        private final Set<String> keys = new HashSet<>();
        private Duration lastTtl;

        @Override
        public boolean markIfAbsent(String gatewayId, String nonce, Duration ttl) {
            lastTtl = ttl;
            return keys.add(gatewayId + ":" + nonce);
        }

        boolean contains(String gatewayId, String nonce) {
            return keys.contains(gatewayId + ":" + nonce);
        }

        Duration lastTtl() {
            return lastTtl;
        }
    }
}
