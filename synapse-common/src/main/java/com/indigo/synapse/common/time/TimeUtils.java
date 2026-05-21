package com.indigo.synapse.common.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    public static Clock utcClock() {
        return Clock.systemUTC();
    }

    public static Instant now(Clock clock) {
        return requireClock(clock).instant();
    }

    public static LocalDate nowDate(Clock clock) {
        return LocalDate.now(requireClock(clock));
    }

    public static LocalDateTime nowDateTime(Clock clock) {
        Clock safeClock = requireClock(clock);
        return LocalDateTime.ofInstant(safeClock.instant(), safeClock.getZone());
    }

    public static Instant toInstant(LocalDateTime dateTime, ZoneId zoneId) {
        return requireDateTime(dateTime).atZone(requireZone(zoneId)).toInstant();
    }

    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        return LocalDateTime.ofInstant(requireInstant(instant), requireZone(zoneId));
    }

    public static LocalDateTime startOfDay(LocalDate date, ZoneId zoneId) {
        return requireDate(date).atStartOfDay(requireZone(zoneId)).toLocalDateTime();
    }

    public static LocalDateTime endOfDay(LocalDate date, ZoneId zoneId) {
        return requireDate(date).atTime(LocalTime.MAX).atZone(requireZone(zoneId)).toLocalDateTime();
    }

    private static Clock requireClock(Clock clock) {
        return Objects.requireNonNull(clock, "clock must not be null");
    }

    private static ZoneId requireZone(ZoneId zoneId) {
        return Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    private static LocalDateTime requireDateTime(LocalDateTime dateTime) {
        return Objects.requireNonNull(dateTime, "dateTime must not be null");
    }

    private static Instant requireInstant(Instant instant) {
        return Objects.requireNonNull(instant, "instant must not be null");
    }

    private static LocalDate requireDate(LocalDate date) {
        return Objects.requireNonNull(date, "date must not be null");
    }
}
