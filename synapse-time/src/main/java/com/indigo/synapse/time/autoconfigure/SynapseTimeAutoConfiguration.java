package com.indigo.synapse.time.autoconfigure;

import com.indigo.synapse.time.DefaultTimeRangeConverter;
import com.indigo.synapse.time.FixedTimeZoneResolver;
import com.indigo.synapse.time.TimeRangeConverter;
import com.indigo.synapse.time.TimeZoneResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;

/**
 * Synapse time 自动配置。
 *
 * <p>只提供时区解析和 UTC 时间范围转换，不提供时区后台、用户资料或业务配置服务。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseTimeProperties.class)
public class SynapseTimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TimeRangeConverter synapseTimeRangeConverter() {
        return new DefaultTimeRangeConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeZoneResolver synapseTimeZoneResolver(SynapseTimeProperties properties) {
        return new FixedTimeZoneResolver(ZoneId.of(properties.getDefaultZone()));
    }
}
