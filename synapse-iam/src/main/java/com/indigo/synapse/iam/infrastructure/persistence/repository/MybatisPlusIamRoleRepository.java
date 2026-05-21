package com.indigo.synapse.iam.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indigo.synapse.iam.domain.model.IamRole;
import com.indigo.synapse.iam.domain.repository.IamRoleRepository;
import com.indigo.synapse.iam.infrastructure.persistence.converter.IamPersistenceConverter;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamRoleEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamRoleMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamUserRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MybatisPlusIamRoleRepository implements IamRoleRepository {

    private final IamRoleMapper iamRoleMapper;
    private final IamUserRoleMapper iamUserRoleMapper;

    public MybatisPlusIamRoleRepository(IamRoleMapper iamRoleMapper, IamUserRoleMapper iamUserRoleMapper) {
        this.iamRoleMapper = iamRoleMapper;
        this.iamUserRoleMapper = iamUserRoleMapper;
    }

    @Override
    public List<IamRole> findEnabledRolesByUserId(String userId) {
        Set<String> roleIds = iamUserRoleMapper.selectList(
                        new LambdaQueryWrapper<com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserRoleEntity>()
                                .eq(com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserRoleEntity::getUserId, userId)
                                .eq(com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserRoleEntity::getDeleted, 0))
                .stream()
                .map(com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserRoleEntity::getRoleId)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return iamRoleMapper.selectList(
                        new LambdaQueryWrapper<IamRoleEntity>()
                                .in(IamRoleEntity::getId, roleIds)
                                .eq(IamRoleEntity::getEnabled, true)
                                .eq(IamRoleEntity::getDeleted, 0))
                .stream()
                .map(IamPersistenceConverter::toDomain)
                .toList();
    }

    @Override
    public Optional<IamRole> findByCode(String code) {
        return Optional.ofNullable(iamRoleMapper.selectOne(
                        new LambdaQueryWrapper<IamRoleEntity>()
                                .eq(IamRoleEntity::getCode, code)
                                .eq(IamRoleEntity::getEnabled, true)
                                .eq(IamRoleEntity::getDeleted, 0)))
                .map(IamPersistenceConverter::toDomain);
    }
}
