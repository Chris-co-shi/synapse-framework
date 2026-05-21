package com.indigo.synapse.iam.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indigo.synapse.iam.domain.model.IamUser;
import com.indigo.synapse.iam.domain.repository.IamUserRepository;
import com.indigo.synapse.iam.infrastructure.persistence.converter.IamPersistenceConverter;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamUserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisPlusIamUserRepository implements IamUserRepository {

    private final IamUserMapper iamUserMapper;

    public MybatisPlusIamUserRepository(IamUserMapper iamUserMapper) {
        this.iamUserMapper = iamUserMapper;
    }

    @Override
    public Optional<IamUser> findByUsername(String username) {
        return Optional.ofNullable(iamUserMapper.selectOne(
                        new LambdaQueryWrapper<IamUserEntity>()
                                .eq(IamUserEntity::getUsername, username)
                                .eq(IamUserEntity::getDeleted, 0)
                ))
                .map(IamPersistenceConverter::toDomain);
    }

    @Override
    public IamUser save(IamUser user) {
        IamUserEntity entity = IamPersistenceConverter.toEntity(user);
        iamUserMapper.insert(entity);
        return IamPersistenceConverter.toDomain(entity);
    }
}
