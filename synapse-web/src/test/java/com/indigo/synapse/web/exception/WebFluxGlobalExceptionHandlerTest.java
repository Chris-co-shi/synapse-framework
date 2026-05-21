package com.indigo.synapse.web.exception;

import com.indigo.synapse.common.error.CommonErrorCode;
import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.web.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebFluxGlobalExceptionHandlerTest {

    private final WebFluxGlobalExceptionHandler handler = new WebFluxGlobalExceptionHandler();

    @Test
    void shouldHandleBusinessException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                new BusinessException(CommonErrorCode.COMMON_CONFLICT, "版本冲突"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("COMMON_CONFLICT", response.getBody().getCode());
    }

    @Test
    void shouldHandleInputMethodAndMediaErrors() {
        assertEquals(400, handler.handleServerWebInputException(new ServerWebInputException("bad input"))
                .getStatusCode().value());
        assertEquals(405, handler.handleMethodNotAllowedException(new MethodNotAllowedException("POST", java.util.List.of()))
                .getStatusCode().value());
        assertEquals(415, handler.handleUnsupportedMediaTypeStatusException(
                new UnsupportedMediaTypeStatusException("application/x-test")).getStatusCode().value());
    }
}
