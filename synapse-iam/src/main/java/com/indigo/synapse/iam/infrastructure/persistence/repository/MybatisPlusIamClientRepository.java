package com.indigo.synapse.iam.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indigo.synapse.iam.domain.model.IamClient;
import com.indigo.synapse.iam.domain.repository.IamClientRepository;
import com.indigo.synapse.iam.infrastructure.persistence.converter.IamPersistenceConverter;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamClientEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamClientMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisPlusIamClientRepository implements IamClientRepository {

    private final IamClientMapper iamClientMapper;

    public MybatisPlusIamClientRepository(IamClientMapper iamClientMapper) {
        this.iamClientMapper = iamClientMapper;
    }

    @Override
    public Optional<IamClient> findByClientId(String clientId) {
        return Optional.ofNullable(iamClientMapper.selectOne(
                        new LambdaQueryWrapper<IamClientEntity>().eq(
                                IamClientEntity::getClientId,
                                clientId
                        ).eq(IamClientEntity::getDeleted, 0)
                ))
                .map(IamPersistenceConverter::toDomain);
    }
}
