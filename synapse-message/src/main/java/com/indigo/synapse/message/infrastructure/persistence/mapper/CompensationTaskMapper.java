package com.indigo.synapse.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indigo.synapse.message.infrastructure.persistence.entity.CompensationTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 补偿任务 Mapper。
 */
@Mapper
public interface CompensationTaskMapper extends BaseMapper<CompensationTaskEntity> {
}
