package com.indigo.synapse.common.id;

/**
 * ID 生成器抽象。
 *
 * <p>core 只定义纯 Java 抽象和默认实现，不绑定数据库、Redis、雪花算法服务或其他外部组件。
 * 需要强一致序列或分布式发号时，应由上层模块提供适配实现。</p>
 */
public interface IdGenerator {

    /**
     * 生成一个新的 ID。
     *
     * @return 非空 ID 字符串
     */
    String generate();
}
