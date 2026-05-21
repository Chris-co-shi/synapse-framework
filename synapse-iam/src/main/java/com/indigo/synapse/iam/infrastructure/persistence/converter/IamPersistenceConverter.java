package com.indigo.synapse.iam.infrastructure.persistence.converter;

import com.indigo.synapse.iam.domain.model.IamClient;
import com.indigo.synapse.iam.domain.model.IamPermission;
import com.indigo.synapse.iam.domain.model.IamRole;
import com.indigo.synapse.iam.domain.model.IamUser;
import com.indigo.synapse.iam.domain.model.IamUserStatus;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamClientEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamPermissionEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamRoleEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserEntity;

public final class IamPersistenceConverter {

    private IamPersistenceConverter() {
    }

    public static IamClient toDomain(IamClientEntity entity) {
        return new IamClient(entity.getId(), entity.getClientId(), Boolean.TRUE.equals(entity.getEnabled()));
    }

    public static IamUser toDomain(IamUserEntity entity) {
        return new IamUser(
                entity.getId(),
                entity.getTenantId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getPasswordHash(),
                IamUserStatus.valueOf(entity.getStatus())
        );
    }

    public static IamRole toDomain(IamRoleEntity entity) {
        return new IamRole(entity.getId(), entity.getCode(), entity.getName(), Boolean.TRUE.equals(entity.getEnabled()));
    }

    public static IamPermission toDomain(IamPermissionEntity entity) {
        return new IamPermission(entity.getId(), entity.getCode(), entity.getName(), Boolean.TRUE.equals(entity.getEnabled()));
    }

    public static IamClientEntity toEntity(IamClient domain) {
        IamClientEntity entity = new IamClientEntity();
        entity.setId(domain.id());
        entity.setClientId(domain.clientId());
        entity.setEnabled(domain.enabled());
        return entity;
    }

    public static IamUserEntity toEntity(IamUser domain) {
        IamUserEntity entity = new IamUserEntity();
        entity.setId(domain.id());
        entity.setTenantId(domain.tenantId());
        entity.setUsername(domain.username());
        entity.setDisplayName(domain.displayName());
        entity.setPasswordHash(domain.passwordHash());
        entity.setStatus(domain.status().name());
        return entity;
    }

    public static IamRoleEntity toEntity(IamRole domain) {
        IamRoleEntity entity = new IamRoleEntity();
        entity.setId(domain.id());
        entity.setCode(domain.code());
        entity.setName(domain.name());
        entity.setEnabled(domain.enabled());
        return entity;
    }

    public static IamPermissionEntity toEntity(IamPermission domain) {
        IamPermissionEntity entity = new IamPermissionEntity();
        entity.setId(domain.id());
        entity.setCode(domain.code());
        entity.setName(domain.name());
        entity.setEnabled(domain.enabled());
        return entity;
    }
}
