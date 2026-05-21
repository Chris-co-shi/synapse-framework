package com.indigo.synapse.iam.application.service;

import com.indigo.synapse.audit.recorder.AuditRecorder;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.indigo.synapse.common.id.IdGenerator;
import com.indigo.synapse.common.id.UuidIdGenerator;
import com.indigo.synapse.iam.domain.repository.IamClientRepository;
import com.indigo.synapse.iam.domain.repository.IamPermissionRepository;
import com.indigo.synapse.iam.domain.repository.IamRoleRepository;
import com.indigo.synapse.iam.domain.repository.IamUserRepository;
import com.indigo.synapse.iam.domain.repository.IamUserRoleRepository;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.jwt.SynapseJwtService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Duration;

@AutoConfiguration
public class IamApplicationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator iamIdGenerator() {
        return UuidIdGenerator.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean
    public IamPermissionApplicationService iamPermissionApplicationService(
            IamRoleRepository roleRepository,
            IamPermissionRepository permissionRepository
    ) {
        return new IamPermissionApplicationService(roleRepository, permissionRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public IamAuthApplicationService iamAuthApplicationService(
            IamClientRepository clientRepository,
            IamUserRepository userRepository,
            IamRoleRepository roleRepository,
            IamUserRoleRepository userRoleRepository,
            IamPermissionApplicationService permissionApplicationService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            SynapseJwtService jwtService,
            SynapseSecurityProperties securityProperties,
            AuditRecorder auditRecorder,
            IdentifierGenerator persistentIdGenerator,
            IdGenerator idGenerator,
            Clock clock
    ) {
        return new IamAuthApplicationService(
                clientRepository,
                userRepository,
                roleRepository,
                userRoleRepository,
                permissionApplicationService,
                passwordEncoder,
                jwtService,
                auditRecorder,
                persistentIdGenerator,
                idGenerator,
                clock,
                securityProperties.getIssuer(),
                securityProperties.getAccessTokenTtl()
        );
    }
}
