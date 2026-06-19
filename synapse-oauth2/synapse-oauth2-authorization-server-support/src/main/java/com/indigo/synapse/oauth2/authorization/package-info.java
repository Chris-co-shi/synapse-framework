/**
 * OAuth2 Authorization Server 的 JWT 签发技术支持。
 *
 * <p>该模块提供 RSA/JWK/JwtEncoder、签名 key provider、受控签发 claims 和 JWT issuer 等机制。
 * 生产环境必须由 Platform 显式提供可持久化、可轮换的签名 key；运行时开发 key 只能在非生产环境
 * 显式开启。</p>
 *
 * <p>本模块不实现登录、RegisteredClient 管理、Authorization Code、Consent、Refresh Token 存储、
 * 用户角色菜单或完整 IAM。私钥只属于签发侧，Resource Server 不得依赖本模块获取私钥。</p>
 */
package com.indigo.synapse.oauth2.authorization;
