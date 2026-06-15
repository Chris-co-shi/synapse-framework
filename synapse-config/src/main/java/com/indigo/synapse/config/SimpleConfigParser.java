package com.indigo.synapse.config;

import java.time.Duration;

/**
 * 默认配置解析器。
 */
public final class SimpleConfigParser implements ConfigParser {

    @Override
    public <T> T parse(String value, Class<T> targetType) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        String trimmed = value.trim();
        Object parsed;
        if (String.class.equals(targetType)) {
            parsed = trimmed;
        } else if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
            parsed = Integer.valueOf(trimmed);
        } else if (Long.class.equals(targetType) || long.class.equals(targetType)) {
            parsed = Long.valueOf(trimmed);
        } else if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
            parsed = Boolean.valueOf(trimmed);
        } else if (Double.class.equals(targetType) || double.class.equals(targetType)) {
            parsed = Double.valueOf(trimmed);
        } else if (Duration.class.equals(targetType)) {
            parsed = Duration.parse(trimmed);
        } else {
            throw new IllegalArgumentException("unsupported config target type: " + targetType.getName());
        }
        return targetType.cast(parsed);
    }
}
