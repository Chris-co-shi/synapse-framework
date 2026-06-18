/**
 * Spring Cloud / OpenFeign 服务间调用技术支撑。
 *
 * <p>该模块把 core {@code OperationContext} 编码为受控 HTTP Header，并提供 Feign 出站拦截器、
 * 远程错误解码和内部调用签名扩展点。只传播最小技术上下文，不传播密码、凭证、raw token、
 * roles、permissions 或业务对象。</p>
 *
 * <p>上下文传播不等于跨服务授权。本模块不是 Gateway、注册中心、配置中心或业务服务 SDK，
 * 无上下文时也不会伪造 system actor。</p>
 */
package com.indigo.synapse.cloud;
