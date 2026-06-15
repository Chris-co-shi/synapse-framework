package com.indigo.synapse.time;

import java.time.ZoneId;
import java.util.Optional;

/**
 * 固定时区解析器。
 */
public final class FixedTimeZoneResolver implements TimeZoneResolver {

    private final ZoneId zoneId;

    public FixedTimeZoneResolver(ZoneId zoneId) {
        this.zoneId = zoneId == null ? ZoneId.of("UTC") : zoneId;
    }

    @Override
    public Optional<ZoneId> resolve() {
        return Optional.of(zoneId);
    }
}
