package com.indigo.synapse.message.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.core.ReliableMessageStatus;
import com.indigo.synapse.message.infrastructure.persistence.converter.ReliableMessagePersistenceConverter;
import com.indigo.synapse.message.infrastructure.persistence.entity.ReliableMessageEntity;
import com.indigo.synapse.message.infrastructure.persistence.mapper.ReliableMessageMapper;
import com.indigo.synapse.message.port.ReliableMessageRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis-Plus 可靠消息仓储实现。
 */
public final class MybatisReliableMessageRepository implements ReliableMessageRepository {

    private final ReliableMessageMapper mapper;
    private final ReliableMessagePersistenceConverter converter;

    public MybatisReliableMessageRepository(ReliableMessageMapper mapper, ReliableMessagePersistenceConverter converter) {
        if (mapper == null || converter == null) {
            throw new IllegalArgumentException("repository dependencies must not be null");
        }
        this.mapper = mapper;
        this.converter = converter;
    }

    @Override
    public void save(ReliableMessage message) {
        mapper.insert(converter.toEntity(message));
    }

    @Override
    public List<ReliableMessage> claimDueMessages(String workerId, Instant now, Instant lockedUntil, int batchSize) {
        if (workerId == null || workerId.isBlank() || now == null || lockedUntil == null || batchSize <= 0) {
            throw new IllegalArgumentException("claim context is invalid");
        }
        List<ReliableMessageEntity> candidates = mapper.selectList(new LambdaQueryWrapper<ReliableMessageEntity>()
                .in(ReliableMessageEntity::getStatus, ReliableMessageStatus.PENDING.name(), ReliableMessageStatus.RETRY.name())
                .le(ReliableMessageEntity::getNextRetryAt, now)
                .and(wrapper -> wrapper.isNull(ReliableMessageEntity::getLockedUntil)
                        .or()
                        .lt(ReliableMessageEntity::getLockedUntil, now))
                .orderByAsc(ReliableMessageEntity::getNextRetryAt)
                .last("limit " + batchSize));
        return candidates.stream()
                .filter(candidate -> claimOne(candidate, workerId, lockedUntil, now))
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public void markSent(String messageId, Instant now) {
        updateStatus(messageId, ReliableMessageStatus.SENT, now, wrapper -> wrapper
                .set(ReliableMessageEntity::getLockedBy, null)
                .set(ReliableMessageEntity::getLockedUntil, null));
    }

    @Override
    public void markRetry(String messageId, int attempt, Instant nextRetryAt, String lastError, Instant now) {
        updateStatus(messageId, ReliableMessageStatus.RETRY, now, wrapper -> wrapper
                .set(ReliableMessageEntity::getAttempt, attempt)
                .set(ReliableMessageEntity::getNextRetryAt, nextRetryAt)
                .set(ReliableMessageEntity::getLastError, lastError)
                .set(ReliableMessageEntity::getLockedBy, null)
                .set(ReliableMessageEntity::getLockedUntil, null));
    }

    @Override
    public void markDeadLetter(String messageId, int attempt, String lastError, Instant now) {
        updateStatus(messageId, ReliableMessageStatus.DLQ, now, wrapper -> wrapper
                .set(ReliableMessageEntity::getAttempt, attempt)
                .set(ReliableMessageEntity::getLastError, lastError)
                .set(ReliableMessageEntity::getLockedBy, null)
                .set(ReliableMessageEntity::getLockedUntil, null));
    }

    @Override
    public void requeue(String messageId, Instant nextRetryAt, Instant now) {
        updateStatus(messageId, ReliableMessageStatus.PENDING, now, wrapper -> wrapper
                .set(ReliableMessageEntity::getNextRetryAt, nextRetryAt)
                .set(ReliableMessageEntity::getLockedBy, null)
                .set(ReliableMessageEntity::getLockedUntil, null)
                .set(ReliableMessageEntity::getLastError, null));
    }

    @Override
    public void markCancelled(String messageId, Instant now) {
        updateStatus(messageId, ReliableMessageStatus.CANCELLED, now, wrapper -> wrapper
                .set(ReliableMessageEntity::getLockedBy, null)
                .set(ReliableMessageEntity::getLockedUntil, null));
    }

    @Override
    public Optional<ReliableMessage> findByMessageId(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        ReliableMessageEntity entity = mapper.selectById(messageId);
        return entity == null ? Optional.empty() : Optional.of(converter.toDomain(entity));
    }

    private boolean claimOne(ReliableMessageEntity candidate, String workerId, Instant lockedUntil, Instant now) {
        int updated = mapper.update(null, new LambdaUpdateWrapper<ReliableMessageEntity>()
                .eq(ReliableMessageEntity::getMessageId, candidate.getMessageId())
                .eq(ReliableMessageEntity::getVersion, candidate.getVersion())
                .in(ReliableMessageEntity::getStatus, ReliableMessageStatus.PENDING.name(), ReliableMessageStatus.RETRY.name())
                .and(wrapper -> wrapper.isNull(ReliableMessageEntity::getLockedUntil)
                        .or()
                        .lt(ReliableMessageEntity::getLockedUntil, now))
                .set(ReliableMessageEntity::getStatus, ReliableMessageStatus.SENDING.name())
                .set(ReliableMessageEntity::getLockedBy, workerId)
                .set(ReliableMessageEntity::getLockedUntil, lockedUntil)
                .set(ReliableMessageEntity::getUpdatedAt, now)
                .set(ReliableMessageEntity::getVersion, candidate.getVersion() + 1));
        if (updated != 1) {
            return false;
        }
        candidate.setStatus(ReliableMessageStatus.SENDING.name());
        candidate.setLockedBy(workerId);
        candidate.setLockedUntil(lockedUntil);
        candidate.setUpdatedAt(now);
        candidate.setVersion(candidate.getVersion() + 1);
        return true;
    }

    private void updateStatus(
            String messageId,
            ReliableMessageStatus status,
            Instant now,
            java.util.function.Function<LambdaUpdateWrapper<ReliableMessageEntity>, LambdaUpdateWrapper<ReliableMessageEntity>> customizer
    ) {
        if (messageId == null || messageId.isBlank() || now == null) {
            throw new IllegalArgumentException("update context is invalid");
        }
        LambdaUpdateWrapper<ReliableMessageEntity> wrapper = new LambdaUpdateWrapper<ReliableMessageEntity>()
                .eq(ReliableMessageEntity::getMessageId, messageId)
                .set(ReliableMessageEntity::getStatus, status.name())
                .set(ReliableMessageEntity::getUpdatedAt, now);
        mapper.update(null, customizer.apply(wrapper));
    }
}
