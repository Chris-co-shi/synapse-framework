/**
 * 运行时配置读取与类型解析抽象。
 *
 * <p>该模块将配置来源、字符串读取和目标类型解析分离。消费方可以通过 {@code ConfigClient}
 * 适配远程配置中心、数据库或其他来源，通过 {@code ConfigParser} 扩展类型转换，而业务代码只依赖
 * 稳定的 {@code ConfigResolver}。</p>
 *
 * <p>配置缺失与配置格式非法是不同语义，不能都吞成 empty。该模块不是配置中心服务端，不提供
 * 发布、审批、灰度、数据库表或管理后台，也不得在异常和日志中泄露敏感配置值。</p>
 */
package com.indigo.synapse.config;
