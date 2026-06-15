package com.indigo.synapse.time;

import java.time.ZoneId;
import java.util.Optional;

/**
 * 当前调用方时区解析端口。
 */
public interface TimeZoneResolver {

    /**
     * 解析当前上下文中的用户时区。
     *
     * @return 当前时区；无法确定时返回 empty
     */
    Optional<ZoneId> resolve();
}
