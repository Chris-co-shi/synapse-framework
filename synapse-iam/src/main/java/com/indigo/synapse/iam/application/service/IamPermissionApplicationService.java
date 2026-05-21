package com.indigo.synapse.iam.application.service;

import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.iam.application.IamErrorCode;
import com.indigo.synapse.iam.domain.model.PermissionSummary;
import com.indigo.synapse.iam.domain.repository.IamPermissionRepository;
import com.indigo.synapse.iam.domain.repository.IamRoleRepository;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class IamPermissionApplicationService {

    private final IamRoleRepository roleRepository;
    private final IamPermissionRepository permissionRepository;

    public IamPermissionApplicationService(IamRoleRepository roleRepository, IamPermissionRepository permissionRepository) {
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository must not be null");
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "permissionRepository must not be null");
    }

    public PermissionSummary loadPermissionSummary(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        var roles = roleRepository.findEnabledRolesByUserId(userId);
        var roleIds = roles.stream()
                .map(role -> role.id())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var roleCodes = roles.stream()
                .map(role -> role.code())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissionCodes = permissionRepository.findEnabledPermissionsByRoleIds(roleIds).stream()
                .map(permission -> permission.code())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new PermissionSummary(roleCodes, permissionCodes);
    }

    public void requirePermission(String userId, String permission) {
        if (!loadPermissionSummary(userId).hasPermission(permission)) {
            throw new BusinessException(IamErrorCode.PERMISSION_DENIED);
        }
    }
}
