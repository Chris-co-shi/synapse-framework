package com.indigo.synapse.iam.domain.repository;

import com.indigo.synapse.iam.domain.model.IamPermission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IamPermissionRepository {

    List<IamPermission> findEnabledPermissionsByRoleIds(Collection<String> roleIds);

    Optional<IamPermission> findByCode(String code);
}
