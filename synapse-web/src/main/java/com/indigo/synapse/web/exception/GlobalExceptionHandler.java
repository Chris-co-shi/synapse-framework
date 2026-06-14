package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.response.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final WebExceptionResponseFactory responseFactory;

    public GlobalExceptionHandler(WebExceptionResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(SynapseException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(SynapseException exception) {
        return response(responseFactory.mvc(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        return response(responseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException exception) {
        return response(responseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return response(responseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        return response(responseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        return response(responseFactory.validation(WebExceptionResponseFactory.MVC_STACK));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception) {
        return response(responseFactory.mvc(exception));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception) {
        return response(responseFactory.mvc(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        return response(responseFactory.mvc(exception));
    }

    private ResponseEntity<Result<Void>> response(WebErrorResponse response) {
        return ResponseEntity.status(response.status())
                .body(response.body());
    }
}