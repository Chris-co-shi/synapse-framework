package com.indigo.synapse.web.exception;

import com.indigo.synapse.web.response.Result;

public record WebErrorResponse(String stack, int status, Result<Void> body) {

    public WebErrorResponse {
        if (stack == null || stack.isBlank()) {
            throw new IllegalArgumentException("stack must not be blank");
        }
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be a valid HTTP status");
        }
        if (body == null) {
            throw new IllegalArgumentException("body must not be null");
        }
    }
}
