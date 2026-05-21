package com.indigo.synapse.security.context;

import java.util.Optional;

public final class SecurityContext {

    private static final ThreadLocal<LoginUser> CURRENT_USER = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(LoginUser loginUser) {
        if (loginUser == null) {
            clear();
            return;
        }
        CURRENT_USER.set(loginUser);
    }

    public static Optional<LoginUser> currentUser() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static void clear() {
        CURRENT_USER.remove();
    }

    public static void clearIfEmpty() {
        if (CURRENT_USER.get() == null) {
            CURRENT_USER.remove();
        }
    }
}
