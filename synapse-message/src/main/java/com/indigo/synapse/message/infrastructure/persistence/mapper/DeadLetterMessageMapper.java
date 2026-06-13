package com.indigo.synapse.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indigo.synapse.message.infrastructure.persistence.entity.DeadLetterMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 死信消息 Mapper。
 */
@Mapper
public interface DeadLetterMessageMapper extends BaseMapper<DeadLetterMessageEntity> {
}
