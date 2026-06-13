package com.indigo.synapse.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.response.Result;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;

import java.util.Set;
import java.lang.reflect.Method;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionShouldUseBusinessErrorCode() {
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(
                new SynapseException(CommonErrorCode.COMMON_CONFLICT, "资源版本冲突"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("COMMON_CONFLICT", response.getBody().getCode());
        assertEquals("资源版本冲突", response.getBody().getMessage());
    }

    @Test
    void bindExceptionShouldReturnBadRequest() {
        ResponseEntity<Result<Void>> response = handler.handleBindException(
                new BindException(new Object(), "request"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("COMMON_BAD_REQUEST", response.getBody().getCode());
        assertEquals("请求参数错误", response.getBody().getMessage());
    }

    @Test
    void constraintViolationExceptionShouldReturnBadRequest() {
        ResponseEntity<Result<Void>> response = handler.handleConstraintViolationException(
                new ConstraintViolationException(Set.of()));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("COMMON_BAD_REQUEST", response.getBody().getCode());
        assertEquals("请求参数错误", response.getBody().getMessage());
    }

    @Test
    void unknownExceptionShouldReturnInternalErrorWithoutStackTrace() {
        ResponseEntity<Result<Void>> response = handler.handleException(new IllegalStateException("boom"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("COMMON_INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("系统内部错误", response.getBody().getMessage());
    }

    @Test
    void missingParameterShouldReturnBadRequest() {
        ResponseEntity<Result<Void>> response = handler.handleMissingServletRequestParameterException(
                new MissingServletRequestParameterException("id", "String"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("COMMON_BAD_REQUEST", response.getBody().getCode());
    }

    @Test
    void typeMismatchShouldReturnBadRequest() {
        ResponseEntity<Result<Void>> response = handler.handleMethodArgumentTypeMismatchException(
                new MethodArgumentTypeMismatchException("x", String.class, "id", methodParameter(), null));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("COMMON_BAD_REQUEST", response.getBody().getCode());
    }

    @Test
    void methodNotAllowedShouldReturn405() {
        ResponseEntity<Result<Void>> response = handler.handleHttpRequestMethodNotSupportedException(
                new HttpRequestMethodNotSupportedException("POST"));

        assertEquals(405, response.getStatusCode().value());
        assertEquals("COMMON_METHOD_NOT_ALLOWED", response.getBody().getCode());
    }

    @Test
    void unsupportedMediaTypeShouldReturn415() {
        ResponseEntity<Result<Void>> response = handler.handleHttpMediaTypeNotSupportedException(
                new HttpMediaTypeNotSupportedException("application/x-test"));

        assertEquals(415, response.getStatusCode().value());
        assertEquals("COMMON_UNSUPPORTED_MEDIA_TYPE", response.getBody().getCode());
    }

    private static MethodParameter methodParameter() {
        try {
            Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("helper", String.class);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings("unused")
    private void helper(String value) {
    }
}
