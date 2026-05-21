package com.indigo.synapse.iam.application.service;

import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.iam.domain.model.IamPermission;
import com.indigo.synapse.iam.domain.model.IamRole;
import com.indigo.synapse.iam.domain.repository.IamPermissionRepository;
import com.indigo.synapse.iam.domain.repository.IamRoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IamPermissionApplicationServiceTest {

    @Test
    void shouldLoadRoleAndPermissionSummary() {
        IamPermissionApplicationService service = service();

        var summary = service.loadPermissionSummary("user-1");

        assertThat(summary.roles()).containsExactly("admin");
        assertThat(summary.permissions()).containsExactly("system:user:list");
    }

    @Test
    void shouldRejectMissingPermission() {
        IamPermissionApplicationService service = service();

        assertThrows(BusinessException.class, () -> service.requirePermission("user-1", "system:user:create"));
    }

    private static IamPermissionApplicationService service() {
        IamRoleRepository roleRepository = new IamRoleRepository() {
            @Override
            public List<IamRole> findEnabledRolesByUserId(String userId) {
                return List.of(new IamRole("role-1", "admin", "管理员", true));
            }

            @Override
            public java.util.Optional<IamRole> findByCode(String code) {
                return java.util.Optional.of(new IamRole("role-1", code, "管理员", true));
            }
        };
        IamPermissionRepository permissionRepository = new IamPermissionRepository() {
            @Override
            public List<IamPermission> findEnabledPermissionsByRoleIds(java.util.Collection<String> roleIds) {
                return List.of(new IamPermission("perm-1", "system:user:list", "用户列表", true));
            }

            @Override
            public java.util.Optional<IamPermission> findByCode(String code) {
                return java.util.Optional.of(new IamPermission("perm-1", code, "用户列表", true));
            }
        };
        return new IamPermissionApplicationService(roleRepository, permissionRepository);
    }
}
