package com.indigo.synapse.core.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 时间处理工具。
 *
 * <p>对外方法优先接收 {@link Clock} 和 {@link ZoneId}，让调用方显式控制时钟和时区，
 * 避免测试受系统默认时区或当前时间影响。</p>
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    /**
     * 返回系统默认时区时钟。
     *
     * @return 系统默认时区时钟
     */
    public static Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    /**
     * 返回 UTC 时钟。
     *
     * @return UTC 时钟
     */
    public static Clock utcClock() {
        return Clock.systemUTC();
    }

    /**
     * 基于指定时钟获取当前瞬时时间。
     *
     * @param clock 时钟，不能为空
     * @return 当前瞬时时间
     */
    public static Instant now(Clock clock) {
        return requireClock(clock).instant();
    }

    /**
     * 基于指定时钟获取当前日期。
     *
     * @param clock 时钟，不能为空
     * @return 当前日期
     */
    public static LocalDate nowDate(Clock clock) {
        return LocalDate.now(requireClock(clock));
    }

    /**
     * 基于指定时钟获取当前本地日期时间。
     *
     * @param clock 时钟，不能为空
     * @return 当前本地日期时间
     */
    public static LocalDateTime nowDateTime(Clock clock) {
        Clock safeClock = requireClock(clock);
        return LocalDateTime.ofInstant(safeClock.instant(), safeClock.getZone());
    }

    /**
     * 将本地日期时间按指定时区转换为瞬时时间。
     *
     * @param dateTime 本地日期时间，不能为空
     * @param zoneId 时区，不能为空
     * @return 瞬时时间
     */
    public static Instant toInstant(LocalDateTime dateTime, ZoneId zoneId) {
        return requireDateTime(dateTime).atZone(requireZone(zoneId)).toInstant();
    }

    /**
     * 将瞬时时间按指定时区转换为本地日期时间。
     *
     * @param instant 瞬时时间，不能为空
     * @param zoneId 时区，不能为空
     * @return 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        return LocalDateTime.ofInstant(requireInstant(instant), requireZone(zoneId));
    }

    /**
     * 返回指定日期在指定时区的当天开始时间。
     *
     * @param date 日期，不能为空
     * @param zoneId 时区，不能为空
     * @return 当天开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date, ZoneId zoneId) {
        return requireDate(date).atStartOfDay(requireZone(zoneId)).toLocalDateTime();
    }

    /**
     * 返回指定日期在指定时区的当天结束时间。
     *
     * @param date 日期，不能为空
     * @param zoneId 时区，不能为空
     * @return 当天结束时间
     */
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
