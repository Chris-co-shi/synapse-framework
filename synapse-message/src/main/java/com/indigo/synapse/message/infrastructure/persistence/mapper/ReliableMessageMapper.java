package com.indigo.synapse.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indigo.synapse.message.infrastructure.persistence.entity.ReliableMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 可靠消息 Mapper。
 */
@Mapper
public interface ReliableMessageMapper extends BaseMapper<ReliableMessageEntity> {
}
