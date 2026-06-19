package com.indigo.synapse.webflux.exception;

import com.indigo.synapse.web.core.response.Result;

public record WebFluxErrorResponse(int status, Result<Void> body) {

    public WebFluxErrorResponse {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be a valid HTTP status");
        }
        if (body == null) {
            throw new IllegalArgumentException("body must not be null");
        }
    }
}
