package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.security.context.AuthenticatedPrincipal;

public final class SecurityContextState {

    private static final ThreadLocal<AuthenticatedPrincipal>
            CURRENT_PRINCIPAL = new ThreadLocal<>();

    private SecurityContextState() {
    }

    public static AuthenticatedPrincipal currentPrincipal() {
        return CURRENT_PRINCIPAL.get();
    }

    static void setPrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            CURRENT_PRINCIPAL.remove();
        } else {
            CURRENT_PRINCIPAL.set(principal);
        }
    }
}