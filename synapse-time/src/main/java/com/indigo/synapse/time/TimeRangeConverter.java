package com.indigo.synapse.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 本地日期时间到 UTC 查询范围的转换器。
 */
public interface TimeRangeConverter {

    /**
     * 将用户时区下的自然日转换为 UTC 查询范围。
     */
    TimeRange dayRange(LocalDate date, ZoneId zoneId);

    /**
     * 将用户时区下的本地时间范围转换为 UTC 查询范围。
     */
    TimeRange range(LocalDateTime startInclusive, LocalDateTime endExclusive, ZoneId zoneId);
}
