package com.indigo.synapse.web.context;

import java.util.Optional;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void set(RequestContext requestContext) {
        if (requestContext == null) {
            clear();
            return;
        }
        REQUEST_CONTEXT.set(requestContext);
    }

    public static Optional<RequestContext> current() {
        return Optional.ofNullable(REQUEST_CONTEXT.get());
    }

    public static void clear() {
        REQUEST_CONTEXT.remove();
    }
}
