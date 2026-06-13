package com.indigo.synapse.message.infrastructure.persistence.repository;

import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.infrastructure.persistence.entity.DeadLetterMessageEntity;
import com.indigo.synapse.message.infrastructure.persistence.mapper.DeadLetterMessageMapper;
import com.indigo.synapse.message.port.DeadLetterRepository;

import java.time.Instant;

/**
 * MyBatis-Plus 死信仓储实现。
 */
public final class MybatisDeadLetterRepository implements DeadLetterRepository {

    private final DeadLetterMessageMapper mapper;

    public MybatisDeadLetterRepository(DeadLetterMessageMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    @Override
    public void save(ReliableMessage message, String reason) {
        DeadLetterMessageEntity entity = new DeadLetterMessageEntity();
        entity.setMessageId(message.messageId());
        entity.setTopic(message.envelope().topic());
        entity.setReason(reason);
        entity.setCreatedAt(Instant.now());
        mapper.insert(entity);
    }
}
