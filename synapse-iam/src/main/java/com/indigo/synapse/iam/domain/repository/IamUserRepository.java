package com.indigo.synapse.iam.domain.repository;

import com.indigo.synapse.iam.domain.model.IamUser;

import java.util.Optional;

public interface IamUserRepository {

    Optional<IamUser> findByUsername(String username);

    IamUser save(IamUser user);
}
