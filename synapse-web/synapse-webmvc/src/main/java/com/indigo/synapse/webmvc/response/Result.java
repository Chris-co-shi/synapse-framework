package com.indigo.synapse.webmvc.response;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.webmvc.trace.TraceContext;
import com.indigo.synapse.webmvc.trace.TraceIdGenerator;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * 统一接口响应结果。
 *
 * <p>该对象用于 Web 层向前端或调用方返回统一格式的数据结构，包含业务状态码、提示消息、
 * 业务数据、链路追踪 ID 以及响应时间。</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *     <li>只表达接口响应结果，不承载异常处理逻辑。</li>
 *     <li>成功与失败响应统一通过静态工厂方法创建。</li>
 *     <li>traceId 和 timestamp 在缺失时自动补齐，避免响应结果缺少追踪信息。</li>
 *     <li>使用 Java record 保持响应模型不可变。</li>
 * </ul>
 *
 * @param code      业务状态码，不能为空
 * @param message   响应消息，为空时默认转为空字符串
 * @param data      响应数据，允许为空
 * @param traceId   链路追踪 ID，为空时从当前上下文获取，若上下文不存在则自动生成
 * @param timestamp 响应创建时间，为空时默认使用当前时间
 * @param <T>       响应数据类型
 */
public record Result<T>(
        String code,
        String message,
        T data,
        String traceId,
        Instant timestamp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规范化响应字段。
     *
     * <p>record 的紧凑构造方法会在所有构造入口执行，因此这里用于统一保证字段约束：</p>
     * <ul>
     *     <li>code 必须存在。</li>
     *     <li>message 为空时转为空字符串。</li>
     *     <li>traceId 为空时自动补齐。</li>
     *     <li>timestamp 为空时自动使用当前时间。</li>
     * </ul>
     */
    public Result {
        code = Objects.requireNonNull(code, "code must not be null");
        message = Objects.requireNonNullElse(message, "");
        traceId = Objects.requireNonNullElseGet(traceId, Result::resolveTraceId);
        timestamp = Objects.requireNonNullElseGet(timestamp, Instant::now);
    }

    /**
     * 创建无响应数据的成功结果。
     *
     * @return 成功响应
     */
    public static Result<Void> success() {
        return success(null);
    }

    /**
     * 创建带响应数据的成功结果。
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(
                CommonErrorCode.SUCCESS.code(),
                CommonErrorCode.SUCCESS.message(),
                data,
                resolveTraceId(),
                Instant.now()
        );
    }

    /**
     * 创建失败结果，响应消息默认使用错误码定义中的消息。
     *
     * @param errorCode 错误码
     * @return 失败响应
     */
    public static Result<Void> fail(ErrorCode errorCode) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return fail(checkedErrorCode, checkedErrorCode.message());
    }

    /**
     * 创建失败结果，并允许覆盖默认错误消息。
     *
     * <p>当 message 为空时，会回退使用 errorCode 中定义的默认消息。</p>
     *
     * @param errorCode 错误码
     * @param message   自定义错误消息
     * @return 失败响应
     */
    public static Result<Void> fail(ErrorCode errorCode, String message) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);

        return new Result<>(
                checkedErrorCode.code(),
                Objects.requireNonNullElse(message, checkedErrorCode.message()),
                null,
                resolveTraceId(),
                Instant.now()
        );
    }

    /**
     * 判断当前响应是否为成功结果。
     *
     * @return 如果响应状态码等于成功状态码，则返回 true
     */
    public boolean isSuccess() {
        return CommonErrorCode.SUCCESS.code().equals(code);
    }

    /**
     * 判断当前响应是否为失败结果。
     *
     * @return 如果响应状态码不等于成功状态码，则返回 true
     */
    public boolean isFailed() {
        return !isSuccess();
    }

    /**
     * 返回一个替换 traceId 后的新响应对象。
     *
     * <p>由于 record 是不可变对象，因此不会修改当前实例，而是创建一个新实例。</p>
     *
     * @param traceId 新的链路追踪 ID
     * @return 替换 traceId 后的新响应对象
     */
    public Result<T> withTraceId(String traceId) {
        return new Result<>(
                this.code,
                this.message,
                this.data,
                traceId,
                this.timestamp
        );
    }

    /**
     * 校验错误码不能为空。
     *
     * @param errorCode 错误码
     * @return 非空错误码
     */
    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    /**
     * 获取当前链路追踪 ID。
     *
     * <p>优先从当前 TraceContext 中获取 traceId；如果当前上下文不存在 traceId，
     * 则生成一个新的 traceId，保证每个响应都具备可追踪性。</p>
     *
     * <p>注意：不能命名为 {@code traceId()}，因为 record 组件 {@code traceId}
     * 会自动生成同名访问器方法 {@code traceId()}。</p>
     *
     * @return 当前链路追踪 ID
     */
    private static String resolveTraceId() {
        return TraceContext.currentTraceId().orElseGet(TraceIdGenerator::generate);
    }
}