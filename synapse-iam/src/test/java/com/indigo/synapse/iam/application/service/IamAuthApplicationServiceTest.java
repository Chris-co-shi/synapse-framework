package com.indigo.synapse.iam.application.service;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.common.id.IdGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.indigo.synapse.iam.application.command.LoginCommand;
import com.indigo.synapse.iam.domain.model.IamClient;
import com.indigo.synapse.iam.domain.model.IamPermission;
import com.indigo.synapse.iam.domain.model.IamRole;
import com.indigo.synapse.iam.domain.model.IamUser;
import com.indigo.synapse.iam.domain.model.IamUserStatus;
import com.indigo.synapse.iam.domain.repository.IamClientRepository;
import com.indigo.synapse.iam.domain.repository.IamPermissionRepository;
import com.indigo.synapse.iam.domain.repository.IamRoleRepository;
import com.indigo.synapse.iam.domain.repository.IamUserRepository;
import com.indigo.synapse.iam.domain.repository.IamUserRoleRepository;
import com.indigo.synapse.security.jwk.SynapseRsaKeyFactory;
import com.indigo.synapse.security.jwt.JwtTokenType;
import com.indigo.synapse.security.jwt.SynapseJwtService;
import com.indigo.synapse.security.password.SynapsePasswordEncoderFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IamAuthApplicationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldLoginAndIssueVerifiableAccessToken() throws Exception {
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        SynapseJwtService jwtService = jwtService();
        IamAuthApplicationService service = service(auditLogPort, jwtService, IamUserStatus.ENABLED, "secret");

        var result = service.login(new LoginCommand("admin-console", "admin", "secret", "trace-1"));

        assertEquals("user-1", result.userId());
        assertEquals(Instant.parse("2030-01-01T00:15:00Z"), result.expiresAt());
        assertThat(result.permissionSummary().roles()).containsExactly("admin");
        assertThat(result.permissionSummary().permissions()).containsExactly("system:user:list");
        var claims = jwtService.verify(result.accessToken());
        assertEquals("user-1", claims.subject());
        assertEquals(JwtTokenType.ACCESS_TOKEN, claims.tokenType());
        assertThat(claims.audience()).containsExactly("admin-console");
        assertThat(auditLogPort.events()).hasSize(1);
        assertEquals(AuditOutcome.SUCCESS, auditLogPort.events().getFirst().outcome());
    }

    @Test
    void shouldRejectUnknownUserAndRecordAudit() throws Exception {
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        IamAuthApplicationService service = serviceWithUserRepository(auditLogPort, new IamUserRepository() {
            @Override
            public Optional<IamUser> findByUsername(String username) {
                return Optional.empty();
            }

            @Override
            public IamUser save(IamUser user) {
                return user;
            }
        });

        assertThrows(BusinessException.class, () -> service.login(new LoginCommand("admin-console", "missing", "secret", "trace-1")));
        assertThat(auditLogPort.events()).hasSize(1);
        assertEquals(AuditOutcome.FAILURE, auditLogPort.events().getFirst().outcome());
    }

    @Test
    void shouldRejectBadPasswordAndRecordAudit() throws Exception {
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        IamAuthApplicationService service = service(auditLogPort, jwtService(), IamUserStatus.ENABLED, "secret");

        assertThrows(BusinessException.class, () -> service.login(new LoginCommand("admin-console", "admin", "bad", "trace-1")));
        assertThat(auditLogPort.events()).hasSize(1);
        assertEquals(AuditOutcome.FAILURE, auditLogPort.events().getFirst().outcome());
    }

    @Test
    void shouldRejectDisabledUserAndRecordAudit() throws Exception {
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        IamAuthApplicationService service = service(auditLogPort, jwtService(), IamUserStatus.DISABLED, "secret");

        assertThrows(BusinessException.class, () -> service.login(new LoginCommand("admin-console", "admin", "secret", "trace-1")));
        assertThat(auditLogPort.events()).hasSize(1);
        assertEquals(AuditOutcome.FAILURE, auditLogPort.events().getFirst().outcome());
    }

    @Test
    void shouldRejectDisabledClientAndRecordAudit() throws Exception {
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        IamAuthApplicationService service = serviceWithClientRepository(auditLogPort, clientId -> Optional.of(new IamClient("client-1", clientId, false)));

        assertThrows(BusinessException.class, () -> service.login(new LoginCommand("admin-console", "admin", "secret", "trace-1")));
        assertThat(auditLogPort.events()).hasSize(1);
        assertEquals(AuditOutcome.FAILURE, auditLogPort.events().getFirst().outcome());
    }

    private static IamAuthApplicationService service(
            RecordingAuditLogPort auditLogPort,
            SynapseJwtService jwtService,
            IamUserStatus status,
            String rawPassword
    ) {
        var passwordEncoder = SynapsePasswordEncoderFactory.bcrypt();
        IamUser user = new IamUser("user-1", "tenant-1", "admin", "管理员", passwordEncoder.encode(rawPassword), status);
        return serviceWithUserRepository(auditLogPort, inMemoryUserRepository(user), jwtService);
    }

    private static IamAuthApplicationService serviceWithUserRepository(
            RecordingAuditLogPort auditLogPort,
            IamUserRepository userRepository
    ) throws Exception {
        return serviceWithUserRepository(auditLogPort, userRepository, jwtService());
    }

    private static IamAuthApplicationService serviceWithUserRepository(
            RecordingAuditLogPort auditLogPort,
            IamUserRepository userRepository,
            SynapseJwtService jwtService
    ) {
        return new IamAuthApplicationService(
                clientId -> Optional.of(new IamClient("client-1", clientId, true)),
                userRepository,
                roleRepository(),
                (userId, roleId) -> {
                },
                permissionService(),
                SynapsePasswordEncoderFactory.bcrypt(),
                jwtService,
                new AuditRecorder(auditLogPort),
                persistentIdGenerator(),
                fixedIdGenerator(),
                CLOCK,
                "synapse",
                Duration.ofMinutes(15)
        );
    }

    private static IamAuthApplicationService serviceWithClientRepository(
            RecordingAuditLogPort auditLogPort,
            IamClientRepository clientRepository
    ) throws Exception {
        var passwordEncoder = SynapsePasswordEncoderFactory.bcrypt();
        IamUser user = new IamUser("user-1", "tenant-1", "admin", "管理员", passwordEncoder.encode("secret"), IamUserStatus.ENABLED);
        return new IamAuthApplicationService(
                clientRepository,
                inMemoryUserRepository(user),
                roleRepository(),
                (userId, roleId) -> {
                },
                permissionService(),
                passwordEncoder,
                jwtService(),
                new AuditRecorder(auditLogPort),
                persistentIdGenerator(),
                fixedIdGenerator(),
                CLOCK,
                "synapse",
                Duration.ofMinutes(15)
        );
    }

    private static IamUserRepository inMemoryUserRepository(IamUser user) {
        Map<String, IamUser> users = new LinkedHashMap<>();
        users.put(user.username(), user);
        return new IamUserRepository() {
            @Override
            public Optional<IamUser> findByUsername(String username) {
                return Optional.ofNullable(users.get(username));
            }

            @Override
            public IamUser save(IamUser savedUser) {
                users.put(savedUser.username(), savedUser);
                return savedUser;
            }
        };
    }

    private static IamRoleRepository roleRepository() {
        return new IamRoleRepository() {
            @Override
            public List<IamRole> findEnabledRolesByUserId(String userId) {
                return List.of(new IamRole("role-1", "admin", "管理员", true));
            }

            @Override
            public Optional<IamRole> findByCode(String code) {
                return Optional.of(new IamRole("role-1", code, "管理员", true));
            }
        };
    }

    private static IamPermissionApplicationService permissionService() {
        IamRoleRepository roleRepository = roleRepository();
        IamPermissionRepository permissionRepository = new IamPermissionRepository() {
            @Override
            public List<IamPermission> findEnabledPermissionsByRoleIds(Collection<String> roleIds) {
                return List.of(new IamPermission("perm-1", "system:user:list", "用户列表", true));
            }

            @Override
            public Optional<IamPermission> findByCode(String code) {
                return Optional.of(new IamPermission("perm-1", code, "用户列表", true));
            }
        };
        return new IamPermissionApplicationService(roleRepository, permissionRepository);
    }

    private static SynapseJwtService jwtService() throws Exception {
        RSAKey rsaKey = SynapseRsaKeyFactory.generate("kid-test");
        return new SynapseJwtService(
                new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey))),
                NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build(),
                "kid-test"
        );
    }

    private static IdGenerator fixedIdGenerator() {
        return () -> "token-1";
    }

    private static IdentifierGenerator persistentIdGenerator() {
        return new IdentifierGenerator() {
            @Override
            public Number nextId(Object entity) {
                return 1234567890123456789L;
            }

            @Override
            public String nextUUID(Object entity) {
                return nextId(entity).toString();
            }
        };
    }

    private static final class RecordingAuditLogPort implements AuditLogPort {

        private final List<AuditEvent> events = new ArrayList<>();

        @Override
        public void record(AuditEvent event) {
            events.add(event);
        }

        List<AuditEvent> events() {
            return events;
        }
    }
}
