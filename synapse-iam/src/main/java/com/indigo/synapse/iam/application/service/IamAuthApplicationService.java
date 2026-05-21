package com.indigo.synapse.iam.application.service;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.common.id.IdGenerator;
import com.indigo.synapse.iam.application.IamErrorCode;
import com.indigo.synapse.iam.application.command.CreateUserCommand;
import com.indigo.synapse.iam.application.command.LoginCommand;
import com.indigo.synapse.iam.application.result.LoginResult;
import com.indigo.synapse.iam.application.result.UserResult;
import com.indigo.synapse.iam.domain.model.IamClient;
import com.indigo.synapse.iam.domain.model.IamRole;
import com.indigo.synapse.iam.domain.model.IamUser;
import com.indigo.synapse.iam.domain.model.IamUserStatus;
import com.indigo.synapse.iam.domain.model.PermissionSummary;
import com.indigo.synapse.iam.domain.repository.IamClientRepository;
import com.indigo.synapse.iam.domain.repository.IamRoleRepository;
import com.indigo.synapse.iam.domain.repository.IamUserRepository;
import com.indigo.synapse.iam.domain.repository.IamUserRoleRepository;
import com.indigo.synapse.security.jwt.JwtClaims;
import com.indigo.synapse.security.jwt.JwtTokenType;
import com.indigo.synapse.security.jwt.SynapseJwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class IamAuthApplicationService {

    public static final String AUDIT_ACTION_LOGIN = "iam.auth.login";
    public static final String AUDIT_ACTION_CREATE_USER = "iam.user.create";

    private final IamClientRepository clientRepository;
    private final IamUserRepository userRepository;
    private final IamRoleRepository roleRepository;
    private final IamUserRoleRepository userRoleRepository;
    private final IamPermissionApplicationService permissionApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final SynapseJwtService jwtService;
    private final AuditRecorder auditRecorder;
    private final IdentifierGenerator persistentIdGenerator;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final String issuer;
    private final Duration accessTokenTtl;

    public IamAuthApplicationService(
            IamClientRepository clientRepository,
            IamUserRepository userRepository,
            IamRoleRepository roleRepository,
            IamUserRoleRepository userRoleRepository,
            IamPermissionApplicationService permissionApplicationService,
            PasswordEncoder passwordEncoder,
            SynapseJwtService jwtService,
            AuditRecorder auditRecorder,
            IdentifierGenerator persistentIdGenerator,
            IdGenerator idGenerator,
            Clock clock,
            String issuer,
            Duration accessTokenTtl
    ) {
        this.clientRepository = Objects.requireNonNull(clientRepository, "clientRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository must not be null");
        this.userRoleRepository = Objects.requireNonNull(userRoleRepository, "userRoleRepository must not be null");
        this.permissionApplicationService = Objects.requireNonNull(permissionApplicationService, "permissionApplicationService must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
        this.jwtService = Objects.requireNonNull(jwtService, "jwtService must not be null");
        this.auditRecorder = Objects.requireNonNull(auditRecorder, "auditRecorder must not be null");
        this.persistentIdGenerator = Objects.requireNonNull(persistentIdGenerator, "persistentIdGenerator must not be null");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be blank");
        }
        if (accessTokenTtl == null || accessTokenTtl.isNegative() || accessTokenTtl.isZero()) {
            throw new IllegalArgumentException("accessTokenTtl must be positive");
        }
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
    }

    @Transactional
    public UserResult createUser(CreateUserCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (userRepository.findByUsername(command.username()).isPresent()) {
            recordUserEvent(command.username(), null, AuditOutcome.FAILURE, "duplicate username", command.roleCodes(), command.traceId());
            throw new BusinessException(IamErrorCode.USER_USERNAME_EXISTS);
        }
        String userId = persistentIdGenerator.nextId(command).toString();
        IamUser savedUser = userRepository.save(new IamUser(
                userId,
                null,
                command.username(),
                command.displayName(),
                passwordEncoder.encode(command.password()),
                IamUserStatus.ENABLED
        ));
        for (String roleCode : command.roleCodes()) {
            IamRole role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> {
                        recordUserEvent(command.username(), savedUser.id(), AuditOutcome.FAILURE, "role missing: " + roleCode, command.roleCodes(), command.traceId());
                        return new BusinessException(IamErrorCode.ROLE_NOT_FOUND);
                    });
            userRoleRepository.bindRole(savedUser.id(), role.id());
        }
        recordUserEvent(command.username(), savedUser.id(), AuditOutcome.SUCCESS, "create user success", command.roleCodes(), command.traceId());
        return new UserResult(savedUser.id(), savedUser.username(), savedUser.displayName(), command.roleCodes());
    }

    public LoginResult login(LoginCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        IamClient client = clientRepository.findByClientId(command.clientId())
                .filter(IamClient::enabled)
                .orElseThrow(() -> failure(command, IamErrorCode.AUTH_INVALID_CLIENT, "client rejected"));
        IamUser user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> failure(command, IamErrorCode.AUTH_INVALID_CREDENTIALS, "user not found"));
        if (!user.canLogin()) {
            throw failure(command, IamErrorCode.AUTH_USER_DISABLED, "user disabled");
        }
        if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
            throw failure(command, IamErrorCode.AUTH_INVALID_CREDENTIALS, "bad credentials");
        }

        PermissionSummary permissionSummary = permissionApplicationService.loadPermissionSummary(user.id());
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(accessTokenTtl);
        String token = jwtService.issue(new JwtClaims(
                issuer,
                user.id(),
                Set.of(client.clientId()),
                idGenerator.generate(),
                JwtTokenType.ACCESS_TOKEN,
                issuedAt,
                expiresAt
        ));
        record(command, user, AuditOutcome.SUCCESS, "login success");
        return new LoginResult(token, expiresAt, user.id(), user.username(), user.displayName(), permissionSummary);
    }

    private BusinessException failure(LoginCommand command, IamErrorCode errorCode, String message) {
        record(command, null, AuditOutcome.FAILURE, message);
        return new BusinessException(errorCode);
    }

    private void record(LoginCommand command, IamUser user, AuditOutcome outcome, String message) {
        String subjectId = user == null ? command.username() : user.id();
        String tenantId = user == null ? null : user.tenantId();
        auditRecorder.record(AuditEvent.builder()
                .action(AUDIT_ACTION_LOGIN)
                .subject(new AuditSubject("iam-user", subjectId, tenantId))
                .target(new AuditTarget("iam-client", command.clientId()))
                .occurredAt(clock.instant())
                .outcome(outcome)
                .traceId(command.traceId())
                .message(message)
                .attributes(Map.of("username", command.username(), "clientId", command.clientId()))
                .build());
    }

    private void recordUserEvent(String username, String userId, AuditOutcome outcome, String message, List<String> roleCodes, String traceId) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("username", username);
        attributes.put("roles", String.join(",", roleCodes));
        auditRecorder.record(AuditEvent.builder()
                .action(AUDIT_ACTION_CREATE_USER)
                .subject(new AuditSubject("iam-user", username, null))
                .target(new AuditTarget("iam-user", userId == null ? username : userId))
                .occurredAt(clock.instant())
                .outcome(outcome)
                .traceId(traceId)
                .message(message)
                .attributes(attributes)
                .build());
    }
}
