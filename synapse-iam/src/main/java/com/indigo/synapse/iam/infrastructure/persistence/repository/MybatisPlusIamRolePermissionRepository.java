package com.indigo.synapse.iam.infrastructure.persistence.repository;

import com.indigo.synapse.iam.domain.repository.IamRolePermissionRepository;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamRolePermissionEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamRolePermissionMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusIamRolePermissionRepository implements IamRolePermissionRepository {

    private final IamRolePermissionMapper iamRolePermissionMapper;

    public MybatisPlusIamRolePermissionRepository(IamRolePermissionMapper iamRolePermissionMapper) {
        this.iamRolePermissionMapper = iamRolePermissionMapper;
    }

    @Override
    public void bindPermission(String roleId, String permissionId) {
        IamRolePermissionEntity entity = new IamRolePermissionEntity();
        entity.setRoleId(roleId);
        entity.setPermissionId(permissionId);
        iamRolePermissionMapper.insert(entity);
    }
}
