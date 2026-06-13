package com.indigo.synapse.message.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.indigo.synapse.message.infrastructure.persistence.entity.CompensationTaskEntity;
import com.indigo.synapse.message.infrastructure.persistence.mapper.CompensationTaskMapper;
import com.indigo.synapse.message.port.CompensationRepository;

import java.time.Instant;

/**
 * MyBatis-Plus 补偿任务仓储实现。
 */
public final class MybatisCompensationRepository implements CompensationRepository {

    private final CompensationTaskMapper mapper;

    public MybatisCompensationRepository(CompensationTaskMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    @Override
    public void save(String compensationId, String messageId, String handlerName, String payload, Instant now) {
        CompensationTaskEntity entity = new CompensationTaskEntity();
        entity.setCompensationId(compensationId);
        entity.setMessageId(messageId);
        entity.setHandlerName(handlerName);
        entity.setPayload(payload);
        entity.setStatus("PENDING");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        mapper.insert(entity);
    }

    @Override
    public void markSucceeded(String compensationId, Instant now) {
        update(compensationId, "SUCCEEDED", null, now);
    }

    @Override
    public void markFailed(String compensationId, String errorMessage, Instant now) {
        update(compensationId, "FAILED", errorMessage, now);
    }

    private void update(String compensationId, String status, String errorMessage, Instant now) {
        mapper.update(null, new LambdaUpdateWrapper<CompensationTaskEntity>()
                .eq(CompensationTaskEntity::getCompensationId, compensationId)
                .set(CompensationTaskEntity::getStatus, status)
                .set(CompensationTaskEntity::getErrorMessage, errorMessage)
                .set(CompensationTaskEntity::getUpdatedAt, now));
    }
}
