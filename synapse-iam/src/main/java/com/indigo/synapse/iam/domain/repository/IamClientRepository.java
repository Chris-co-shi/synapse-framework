package com.indigo.synapse.iam.domain.repository;

import com.indigo.synapse.iam.domain.model.IamClient;

import java.util.Optional;

public interface IamClientRepository {

    Optional<IamClient> findByClientId(String clientId);
}
