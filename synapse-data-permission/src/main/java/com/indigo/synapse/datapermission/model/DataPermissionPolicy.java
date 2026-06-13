package com.indigo.synapse.datapermission.model;

import java.util.Set;

public record DataPermissionPolicy(
        DataPermissionScope scope,
        Set<String> departmentIds
) {

    public DataPermissionPolicy {
        if (scope == null) {
            throw new IllegalArgumentException("scope must not be null");
        }
        departmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
        if (scope == DataPermissionScope.CUSTOM_DEPT && departmentIds.isEmpty()) {
            throw new IllegalArgumentException("departmentIds must not be empty when scope is CUSTOM_DEPT");
        }
        if (scope != DataPermissionScope.CUSTOM_DEPT && !departmentIds.isEmpty()) {
            throw new IllegalArgumentException("departmentIds must be empty when scope is not CUSTOM_DEPT");
        }
    }

    public static DataPermissionPolicy all() {
        return new DataPermissionPolicy(DataPermissionScope.ALL, Set.of());
    }
}
