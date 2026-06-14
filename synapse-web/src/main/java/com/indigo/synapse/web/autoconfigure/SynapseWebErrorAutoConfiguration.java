package com.indigo.synapse.web.autoconfigure;

import com.indigo.synapse.web.exception.CommonErrorHttpStatusResolver;
import com.indigo.synapse.web.exception.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.web.exception.ErrorHttpStatusResolver;
import com.indigo.synapse.web.exception.WebExceptionResponseFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author 史偕成
 * @date 2026/06/14 12:30
 **/
@AutoConfiguration
public class SynapseWebErrorAutoConfiguration {

    @Bean
    public CommonErrorHttpStatusResolver commonErrorHttpStatusResolver() {
        return new CommonErrorHttpStatusResolver();
    }

    @Bean
    public CompositeErrorHttpStatusResolver compositeErrorHttpStatusResolver(
            List<ErrorHttpStatusResolver> resolvers) {
        return new CompositeErrorHttpStatusResolver(resolvers);
    }

    @Bean
    public WebExceptionResponseFactory webExceptionResponseFactory(
            CompositeErrorHttpStatusResolver statusResolver) {
        return new WebExceptionResponseFactory(statusResolver);
    }
}
