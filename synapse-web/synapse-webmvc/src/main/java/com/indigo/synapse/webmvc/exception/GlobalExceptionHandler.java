package com.indigo.synapse.webmvc.exception;

import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.webmvc.response.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Servlet MVC 全局异常处理器。
 *
 * <p>该处理器只负责进入 DispatcherServlet 之后的异常，例如 Controller、参数绑定、消息转换、
 * MVC 资源匹配等阶段的异常。Servlet Filter 阶段的异常由 {@link SynapseExceptionBridgeFilter}
 * 在更外层桥接。</p>
 *
 * <p>本类不直接拼装错误响应，而是委托 {@link WebExceptionResponseFactory}，保证 MVC 阶段和
 * Filter 阶段返回一致的 {@link Result} 结构。</p>
 */
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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException exception) {
        return response(responseFactory.mvc(exception));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(
            NoResourceFoundException exception) {
        return response(responseFactory.mvc(exception));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        return response(responseFactory.mvc(exception));
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

    /**
     * 处理未被更具体分支覆盖的异常。
     *
     * <p>该分支会返回通用 500 响应。新增常见 Web 异常时，应优先增加更具体映射，
     * 避免客户端错误被误判为服务端错误。</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        return response(responseFactory.mvc(exception));
    }

    private ResponseEntity<Result<Void>> response(WebErrorResponse response) {
        return ResponseEntity.status(response.status())
                .body(response.body());
    }
}
