package com.indigo.synapse.web.exception;

import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.web.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return response(WebExceptionResponseFactory.mvc(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        return response(WebExceptionResponseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return response(WebExceptionResponseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return response(WebExceptionResponseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        return response(WebExceptionResponseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        return response(WebExceptionResponseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception) {
        return response(WebExceptionResponseFactory.mvc(exception));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception) {
        return response(WebExceptionResponseFactory.mvc(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return response(WebExceptionResponseFactory.mvc(exception));
    }

    private ResponseEntity<ApiResponse<Void>> response(WebErrorResponse response) {
        return ResponseEntity.status(response.status())
                .body(response.body());
    }
}
