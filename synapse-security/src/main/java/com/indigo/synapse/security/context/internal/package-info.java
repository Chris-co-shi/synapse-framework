/**
 * SecurityContext 的 Framework 内部绑定与生命周期实现。
 *
 * <p>该包不是面向业务应用的受支持 API。业务代码只能通过
 * {@link com.indigo.synapse.security.context.SecurityContext}
 * 读取当前认证主体，不应直接绑定、替换或恢复认证身份。</p>
 *
 * <p>该包中的部分类型为了支持 Synapse Framework 各 Maven
 * 模块协作而具有 public 可见性；public 不代表业务应用 API。</p>
 */
package com.indigo.synapse.security.context.internal;