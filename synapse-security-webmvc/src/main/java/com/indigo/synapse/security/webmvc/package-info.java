/**
 * trusted-header 协议的 Servlet MVC 适配层。
 *
 * <p>该模块在 Servlet Filter 生命周期中读取并校验可信 Header，将其恢复为 Synapse 已认证主体，
 * 并通过 {@code SecurityContextBinder.bind(...)} 管理当前请求的 SecurityContext 与 OperationContext。
 * 请求正常结束或异常退出时都必须关闭 Scope。</p>
 *
 * <p>trusted-header 只适用于已经建立网络隔离、入口访问控制和共享密钥治理的可信链路，不能替代
 * OAuth2 Bearer Token 验证。与 Resource Server 同时启用时必须避免两个权威身份源。</p>
 */
package com.indigo.synapse.security.webmvc;
