package com.indigo.synapse.iam.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indigo.synapse.iam.domain.model.IamPermission;
import com.indigo.synapse.iam.domain.repository.IamPermissionRepository;
import com.indigo.synapse.iam.infrastructure.persistence.converter.IamPersistenceConverter;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamPermissionEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamRolePermissionEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamPermissionMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamRolePermissionMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MybatisPlusIamPermissionRepository implements IamPermissionRepository {

    private final IamPermissionMapper iamPermissionMapper;
    private final IamRolePermissionMapper iamRolePermissionMapper;

    public MybatisPlusIamPermissionRepository(
            IamPermissionMapper iamPermissionMapper,
            IamRolePermissionMapper iamRolePermissionMapper
    ) {
        this.iamPermissionMapper = iamPermissionMapper;
        this.iamRolePermissionMapper = iamRolePermissionMapper;
    }

    @Override
    public List<IamPermission> findEnabledPermissionsByRoleIds(Collection<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        Set<String> permissionIds = iamRolePermissionMapper.selectList(
                        new LambdaQueryWrapper<IamRolePermissionEntity>()
                                .in(IamRolePermissionEntity::getRoleId, roleIds)
                                .eq(IamRolePermissionEntity::getDeleted, 0))
                .stream()
                .map(IamRolePermissionEntity::getPermissionId)
                .collect(Collectors.toSet());
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return iamPermissionMapper.selectList(
                        new LambdaQueryWrapper<IamPermissionEntity>()
                                .in(IamPermissionEntity::getId, permissionIds)
                                .eq(IamPermissionEntity::getEnabled, true)
                                .eq(IamPermissionEntity::getDeleted, 0))
                .stream()
                .map(IamPersistenceConverter::toDomain)
                .toList();
    }

    @Override
    public Optional<IamPermission> findByCode(String code) {
        return Optional.ofNullable(iamPermissionMapper.selectOne(
                        new LambdaQueryWrapper<IamPermissionEntity>()
                                .eq(IamPermissionEntity::getCode, code)
                                .eq(IamPermissionEntity::getEnabled, true)
                                .eq(IamPermissionEntity::getDeleted, 0)))
                .map(IamPersistenceConverter::toDomain);
    }
}
