/**
 * OAuth2 Resource Server 技术栈无关验证语义。
 *
 * <p>该模块统一 JWT 验证策略、主体映射、authority 映射和认证失败模型。MVC 与 WebFlux
 * 适配器只负责把 Spring Security 类型桥接到这些稳定契约。该包不得包含 Servlet、Spring
 * MVC、Reactor 或 WebFlux 类型。</p>
 */
package com.indigo.synapse.oauth2.resource.core;
