package com.indigo.synapse.mybatisplus.page;

import java.util.Map;
import java.util.Optional;

/**
 * 基于不可变映射的排序字段白名单解析器。
 *
 * <p>该类型维护“外部字段名 -> 数据库列名”的白名单，适合业务系统在查询入口显式声明允许排序的字段。
 * 映射在构造时复制，后续解析过程只读，因此可以安全地作为单例复用。</p>
 *
 * <p>该类型不校验数据库列是否真实存在，也不负责将驼峰字段自动转换为下划线字段，所有映射必须由调用方
 * 明确提供，避免隐式规则扩大 SQL 注入面。</p>
 */
public final class SortFieldMapping implements SortFieldResolver {

    private final Map<String, String> mappings;

    /**
     * 创建排序字段映射。
     *
     * @param mappings 外部字段名到安全数据库列名的映射；null 会被视为空映射
     */
    public SortFieldMapping(Map<String, String> mappings) {
        this.mappings = mappings == null ? Map.of() : Map.copyOf(mappings);
    }

    @Override
    public Optional<String> resolve(String requestedField) {
        if (requestedField == null || requestedField.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mappings.get(requestedField));
    }

    /**
     * 快速创建不可变字段映射。
     *
     * @param mappings 外部字段名到数据库列名的映射
     * @return 字段映射解析器
     */
    public static SortFieldMapping of(Map<String, String> mappings) {
        return new SortFieldMapping(mappings);
    }
}
