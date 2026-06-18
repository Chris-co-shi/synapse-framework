/**
 * Web 无关的安全主体、上下文和权限检查基础。
 *
 * <p>该模块区分 USER 与 CLIENT 主体，并通过可关闭的 SecurityContext Scope 将当前主体单向适配为
 * core OperationContext。线程池、任务、消息消费和异步执行不会自动继承 ThreadLocal，入口必须显式
 * 恢复并在结束后清理。</p>
 *
 * <p>本模块不实现登录、用户角色菜单后台、OAuth2 Resource Server、Servlet Filter、ABAC 或
 * DataScope。默认 PermissionChecker 只检查当前主体携带的 permissions 快照。</p>
 */
package com.indigo.synapse.security;
