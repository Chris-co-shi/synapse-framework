package com.indigo.synapse.iam.domain.repository;

public interface IamRolePermissionRepository {

    void bindPermission(String roleId, String permissionId);
}
