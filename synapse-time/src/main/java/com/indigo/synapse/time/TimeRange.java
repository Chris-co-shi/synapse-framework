package com.indigo.synapse.time;

import java.time.Instant;

/**
 * UTC 查询时间范围。
 *
 * @param startInclusive 起始时间，包含
 * @param endExclusive 结束时间，不包含
 */
public record TimeRange(Instant startInclusive, Instant endExclusive) {

    public TimeRange {
        if (startInclusive == null) {
            throw new IllegalArgumentException("startInclusive must not be null");
        }
        if (endExclusive == null) {
            throw new IllegalArgumentException("endExclusive must not be null");
        }
        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("startInclusive must be before endExclusive");
        }
    }
}
