/**
 * Servlet OAuth2 Resource Server 技术适配层。
 *
 * <p>该模块负责 JWT 验证配置、claims 到 USER/CLIENT 主体和 Spring Authentication 的转换、
 * Spring SecurityContext 到 Synapse SecurityContext/OperationContext 的桥接，以及统一 401/403 响应。
 * Bridge Filter 必须在 Bearer Token 认证完成后执行，并在请求结束时关闭 Scope。</p>
 *
 * <p>本模块不签发 JWT、不持有私钥，也不实现登录、客户端管理、Refresh Token 或完整 IAM。
 * CLIENT 主体不得被映射为 USER，401 与 403 语义不得合并。</p>
 */
package com.indigo.synapse.oauth2.resource.webmvc;
