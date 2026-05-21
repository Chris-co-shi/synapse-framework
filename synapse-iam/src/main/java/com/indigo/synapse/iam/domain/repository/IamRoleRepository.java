package com.indigo.synapse.iam.domain.repository;

import com.indigo.synapse.iam.domain.model.IamRole;

import java.util.List;
import java.util.Optional;

public interface IamRoleRepository {

    List<IamRole> findEnabledRolesByUserId(String userId);

    Optional<IamRole> findByCode(String code);
}
