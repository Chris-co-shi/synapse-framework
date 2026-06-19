package com.indigo.synapse.datasource.definition;

import java.util.Arrays;

/**
 * 短生命周期数据源凭据。
 *
 * <p>密码以字符数组保存，调用方使用完成后应调用 {@link #clear()}。本类型及字段禁止日志输出。</p>
 */
public final class DatasourceCredentials {

    private final String username;
    private final char[] password;

    public DatasourceCredentials(String username, char[] password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        this.username = username;
        this.password = password == null ? new char[0] : password.clone();
    }

    /** @return 用户名 */
    public String username() {
        return username;
    }

    /** @return 密码副本；调用方负责清理 */
    public char[] password() {
        return password.clone();
    }

    /** 用零覆盖内部密码数组。 */
    public void clear() {
        Arrays.fill(password, '\0');
    }
}
