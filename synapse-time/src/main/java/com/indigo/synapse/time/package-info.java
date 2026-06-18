/**
 * 时间与时区技术支撑。
 *
 * <p>真实时间点使用 {@link java.time.Instant}，业务日期使用 {@link java.time.LocalDate}，
 * 业务发生地或调用方时区使用显式 IANA {@link java.time.ZoneId}。日期查询应先确定时区，再转换为
 * UTC 的半开区间 {@code [startInclusive, endExclusive)}。</p>
 *
 * <p>禁止使用 JVM 默认时区解释业务时间，也不要用 {@link java.time.LocalDateTime} 表示跨时区
 * 真实时间点。该模块只提供转换与解析 Port，不管理用户、工厂或组织的时区业务数据。</p>
 */
package com.indigo.synapse.time;
