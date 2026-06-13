package com.indigo.synapse.web.exception;

import com.indigo.synapse.common.exception.SynapseException;
import com.indigo.synapse.web.response.Result;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@RestControllerAdvice
@Order(-2)
public class WebFluxGlobalExceptionHandler {

    @ExceptionHandler(SynapseException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(SynapseException exception) {
        return response(WebExceptionResponseFactory.webflux(exception));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Result<Void>> handleServerWebInputException(ServerWebInputException exception) {
        return response(WebExceptionResponseFactory.validation(WebExceptionResponseFactory.WEBFLUX_STACK));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotAllowedException(MethodNotAllowedException exception) {
        return response(WebExceptionResponseFactory.webflux(exception));
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public ResponseEntity<Result<Void>> handleUnsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException exception) {
        return response(WebExceptionResponseFactory.webflux(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        return response(WebExceptionResponseFactory.webflux(exception));
    }

    private ResponseEntity<Result<Void>> response(WebErrorResponse response) {
        return ResponseEntity.status(response.status())
                .body(response.body());
    }
}
