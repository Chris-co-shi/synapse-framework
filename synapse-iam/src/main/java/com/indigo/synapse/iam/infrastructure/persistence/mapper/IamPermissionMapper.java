package com.indigo.synapse.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamPermissionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IamPermissionMapper extends BaseMapper<IamPermissionEntity> {
}
