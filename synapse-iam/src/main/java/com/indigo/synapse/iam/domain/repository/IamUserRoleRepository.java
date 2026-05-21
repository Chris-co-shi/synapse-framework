package com.indigo.synapse.iam.domain.repository;

public interface IamUserRoleRepository {

    void bindRole(String userId, String roleId);
}
