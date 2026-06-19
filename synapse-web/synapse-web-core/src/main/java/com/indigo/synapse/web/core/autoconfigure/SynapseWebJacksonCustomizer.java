package com.indigo.synapse.web.core.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * Synapse Web 的 Jackson Builder 默认定制。
 *
 * <p>该定制器以较高优先级先执行，Spring Boot 的 {@code spring.jackson.*} 和消费方
 * 默认顺序 Customizer 可在其后覆盖这些默认值。</p>
 */
public final class SynapseWebJacksonCustomizer
        implements Jackson2ObjectMapperBuilderCustomizer, Ordered {

    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    /**
     * 应用不绑定具体 Web 技术栈的 JSON 默认规则。
     *
     * @param builder Spring Boot 管理的 ObjectMapper Builder
     */
    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        builder.featuresToDisable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                )
                .serializationInclusion(JsonInclude.Include.ALWAYS)
                .timeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
