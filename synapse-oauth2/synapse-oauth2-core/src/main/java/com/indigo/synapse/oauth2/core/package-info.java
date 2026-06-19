/**
 * Web 与 Spring Security Config 无关的 OAuth2/JWT 基础契约。
 *
 * <p>该模块定义稳定 claim 名称、token type、validator、denylist Port 和 Bearer Token 读取端口，
 * 供签发端、Servlet/Reactive Resource Server 与 Cloud 适配器共享。</p>
 *
 * <p>该模块不创建私钥、JWKSource、JwtEncoder、SecurityFilterChain 或 Synapse 安全主体。Bearer Token
 * 是凭证，不得写入 OperationContext、MQ Header、普通日志或审计 attributes。</p>
 */
package com.indigo.synapse.oauth2.core;
