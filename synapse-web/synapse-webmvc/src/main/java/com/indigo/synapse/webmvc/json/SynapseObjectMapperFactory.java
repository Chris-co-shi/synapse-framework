package com.indigo.synapse.webmvc.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * Synapse WebMVC 默认 ObjectMapper 工厂。
 *
 * <p>该工厂提供统一 JSON 序列化基础规则：支持 Java Time、时间使用 ISO 字符串、忽略未知字段、
 * 默认保留 null 字段，并统一使用 UTC 时区。消费方如需完全自定义 ObjectMapper，可以提供自己的 Bean。</p>
 */
public final class SynapseObjectMapperFactory {

    private SynapseObjectMapperFactory() {
    }

    /**
     * 创建默认 ObjectMapper。
     */
    public static ObjectMapper create() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        return objectMapper;
    }
}
