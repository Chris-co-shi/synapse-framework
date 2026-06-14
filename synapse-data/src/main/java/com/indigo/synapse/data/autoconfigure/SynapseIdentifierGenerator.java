package com.indigo.synapse.data.autoconfigure;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * Synapse 默认 MyBatis-Plus ID 生成器。
 *
 * <p>该实现委托 MyBatis-Plus {@link DefaultIdentifierGenerator}，适合作为通用技术默认值。
 * 业务系统如需要数据库序列、雪花节点定制、业务单号或外部发号服务，应提供自己的
 * {@link IdentifierGenerator} Bean 覆盖。</p>
 */
public final class SynapseIdentifierGenerator implements IdentifierGenerator {

    private final DefaultIdentifierGenerator delegate = DefaultIdentifierGenerator.getInstance();

    /**
     * 生成数字 ID。
     */
    @Override
    public Number nextId(Object entity) {
        return delegate.nextId(entity);
    }

    /**
     * 生成字符串 ID。
     *
     * <p>当前返回数字 ID 的字符串形式，不生成标准 UUID。</p>
     */
    @Override
    public String nextUUID(Object entity) {
        return nextId(entity).toString();
    }
}
