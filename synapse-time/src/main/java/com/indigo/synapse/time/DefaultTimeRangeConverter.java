package com.indigo.synapse.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 默认 UTC 时间范围转换器。
 */
public final class DefaultTimeRangeConverter implements TimeRangeConverter {

    @Override
    public TimeRange dayRange(LocalDate date, ZoneId zoneId) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        return range(date.atStartOfDay(), date.plusDays(1).atStartOfDay(), zoneId);
    }

    @Override
    public TimeRange range(LocalDateTime startInclusive, LocalDateTime endExclusive, ZoneId zoneId) {
        if (startInclusive == null) {
            throw new IllegalArgumentException("startInclusive must not be null");
        }
        if (endExclusive == null) {
            throw new IllegalArgumentException("endExclusive must not be null");
        }
        ZoneId safeZone = zoneId == null ? ZoneId.of("UTC") : zoneId;
        return new TimeRange(
                startInclusive.atZone(safeZone).toInstant(),
                endExclusive.atZone(safeZone).toInstant()
        );
    }
}
