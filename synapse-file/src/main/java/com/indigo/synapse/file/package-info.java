/**
 * 文件内容存储抽象。
 *
 * <p>该模块定义 FileStorage、保存命令、存储元数据和读取内容流，并提供开发/测试可用的本地文件系统
 * 实现。业务附件关系、上传下载 API、权限、访问 URL、预览和对象存储 SDK 属于业务系统或 Platform
 * 文件服务。</p>
 *
 * <p>objectKey 必须经过规范化和根目录逃逸检查。{@code StoredFile} 返回的 InputStream 由调用方关闭；
 * LocalFileStorage 只适合单机轻量环境，不能被描述为多实例共享生产存储。</p>
 */
package com.indigo.synapse.file;
