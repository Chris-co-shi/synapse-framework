package com.indigo.synapse.iam.infrastructure.persistence.repository;

import com.indigo.synapse.iam.domain.repository.IamUserRoleRepository;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserRoleEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamUserRoleMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusIamUserRoleRepository implements IamUserRoleRepository {

    private final IamUserRoleMapper iamUserRoleMapper;

    public MybatisPlusIamUserRoleRepository(IamUserRoleMapper iamUserRoleMapper) {
        this.iamUserRoleMapper = iamUserRoleMapper;
    }

    @Override
    public void bindRole(String userId, String roleId) {
        IamUserRoleEntity entity = new IamUserRoleEntity();
        entity.setUserId(userId);
        entity.setRoleId(roleId);
        iamUserRoleMapper.insert(entity);
    }
}
