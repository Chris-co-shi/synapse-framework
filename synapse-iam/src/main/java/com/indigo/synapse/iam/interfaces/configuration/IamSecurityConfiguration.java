package com.indigo.synapse.iam.interfaces.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class IamSecurityConfiguration {

    @Bean
    @Order(0)
    public SecurityFilterChain iamAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/admin/auth/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
