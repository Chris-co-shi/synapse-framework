package com.indigo.synapse.security.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器工厂。
 *
 * <p>该工厂只使用 spring-security-crypto 提供 BCryptPasswordEncoder，不引入 spring-security-web、
 * SecurityFilterChain 或登录认证能力。业务系统如果需要自定义强度、迁移策略或多算法兼容，应提供自己的
 * PasswordEncoder Bean。</p>
 */
public final class SynapsePasswordEncoderFactory {

    private SynapsePasswordEncoderFactory() {
    }

    /**
     * 创建 BCrypt 密码编码器。
     */
    public static PasswordEncoder bcrypt() {
        return new BCryptPasswordEncoder();
    }
}
