package com.indigo.synapse.example.foundation;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import com.indigo.synapse.cache.key.CacheKey;
import com.indigo.synapse.cache.lock.LockAcquireResult;
import com.indigo.synapse.cache.lock.LockReleaseResult;
import com.indigo.synapse.cache.lock.RedisReentrantLock;
import com.indigo.synapse.cache.ratelimit.RateLimitDecision;
import com.indigo.synapse.cache.ratelimit.SlidingWindowRateLimiter;
import com.indigo.synapse.data.datasource.DataSourceContext;
import com.indigo.synapse.data.datasource.DataSourceScope;
import com.indigo.synapse.data.dialect.DatabaseDialect;
import com.indigo.synapse.data.dialect.DatabaseDialectResolver;
import com.indigo.synapse.security.context.LoginUser;
import com.indigo.synapse.security.jwk.JwkKeyDescriptor;
import com.indigo.synapse.security.jwt.JwtClaims;
import com.indigo.synapse.security.jwt.JwtTokenType;
import com.indigo.synapse.security.oauth2.OAuth2PublicEndpointPolicy;
import com.indigo.synapse.starter.autoconfigure.SynapseAutoConfigurationPlan;
import com.indigo.synapse.starter.properties.SynapseFeature;
import com.indigo.synapse.web.response.ApiResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class ExampleFoundationScenario {

    public ExampleResult run() {
        SynapseAutoConfigurationPlan plan = SynapseAutoConfigurationPlan.defaults();
        ApiResponse<String> response = ApiResponse.success("foundation-ready");
        CacheKey cacheKey = CacheKey.of("example", "lock", "demo", "tenant-a", "order-1");
        LoginUser loginUser = new LoginUser(
                "1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("example:demo:read")
        );
        ExampleDataUsage dataUsage = dataUsage();
        ExampleCacheUsage cacheUsage = cacheUsage(cacheKey);
        ExampleSecurityUsage securityUsage = securityUsage(loginUser);

        AtomicReference<AuditEvent> recorded = new AtomicReference<>();
        AuditRecorder recorder = new AuditRecorder(recorded::set);
        AuditEvent event = new AuditEvent(
                "example:demo:read",
                new AuditSubject("USER", loginUser.userId(), loginUser.tenantId()),
                new AuditTarget("DEMO", "order-1"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                "trace-example",
                "foundation scenario",
                Map.of("accessToken", "must-mask", "cacheName", cacheKey.value())
        );
        recorder.record(event);

        return new ExampleResult(
                plan.shouldConfigure(SynapseFeature.WEB),
                plan.shouldConfigure(SynapseFeature.DATA),
                plan.shouldConfigure(SynapseFeature.CACHE),
                plan.shouldConfigure(SynapseFeature.SECURITY),
                plan.shouldConfigure(SynapseFeature.AUDIT),
                plan.shouldCreateExternalConnection(SynapseFeature.CACHE),
                response.getCode(),
                cacheKey.value(),
                dataUsage,
                cacheUsage,
                securityUsage,
                recorded.get()
        );
    }

    private static ExampleDataUsage dataUsage() {
        DataSourceContext.use("primary");
        try (DataSourceScope ignored = DataSourceContext.scope("reporting")) {
            DatabaseDialect dialect = DatabaseDialectResolver.fromJdbcUrl("jdbc:postgresql://example/synapse");
            return new ExampleDataUsage(
                    DataSourceContext.current().orElse(""),
                    "primary",
                    dialect.databaseType().name(),
                    dialect.supportsJsonColumn()
            );
        } finally {
            DataSourceContext.clear();
        }
    }

    private static ExampleCacheUsage cacheUsage(CacheKey cacheKey) {
        ExampleRedisScriptExecutor scriptExecutor = new ExampleRedisScriptExecutor();
        RedisReentrantLock lock = new RedisReentrantLock(scriptExecutor);
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(scriptExecutor);

        LockAcquireResult acquire = lock.acquire(cacheKey.value(), "example-owner", Duration.ofSeconds(30));
        LockAcquireResult reenter = lock.acquire(cacheKey.value(), "example-owner", Duration.ofSeconds(30));
        LockReleaseResult release = lock.release(cacheKey.value(), "example-owner", Duration.ofSeconds(30));
        RateLimitDecision decision = rateLimiter.allow(
                CacheKey.of("example", "rate-limit", "demo", "tenant-a", "admin").value(),
                5,
                Duration.ofMinutes(1),
                1_000L
        );
        return new ExampleCacheUsage(
                acquire.acquired(),
                reenter.acquired(),
                release.released(),
                decision.allowed(),
                decision.remaining()
        );
    }

    private static ExampleSecurityUsage securityUsage(LoginUser loginUser) {
        Instant issuedAt = Instant.parse("2026-05-20T10:00:00Z");
        JwtClaims claims = new JwtClaims(
                "synapse-example",
                loginUser.userId(),
                Set.of("synapse-example"),
                "example-token-id",
                JwtTokenType.ACCESS_TOKEN,
                issuedAt,
                issuedAt.plus(Duration.ofMinutes(30))
        );
        JwkKeyDescriptor keyDescriptor = new JwkKeyDescriptor(
                "example-key",
                "RS256",
                "sig",
                issuedAt,
                issuedAt.plus(Duration.ofDays(30))
        );
        return new ExampleSecurityUsage(
                claims.subject(),
                claims.tokenType().name(),
                keyDescriptor.keyId(),
                claims.isExpired(issuedAt.plus(Duration.ofMinutes(1))),
                OAuth2PublicEndpointPolicy.isPublic("/oauth2/token"),
                loginUser.hasPermission("example:demo:read")
        );
    }
}
