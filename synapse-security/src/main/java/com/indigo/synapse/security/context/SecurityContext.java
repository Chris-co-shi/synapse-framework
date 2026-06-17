package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextSnapshot;

import java.util.Optional;

/**
 * 当前线程安全上下文。
 *
 * <p>SecurityContext 保存当前请求或当前执行作用域中的 {@link AuthenticatedUser}。设置用户时，
 * 会同步把用户适配为 core 的 {@link OperationContext}，使 data、audit、mq 等模块可以通过
 * OperationContext 读取操作人信息，而不需要依赖 security。</p>
 *
 * <p>该类型基于 ThreadLocal，使用 Servlet 线程池、异步执行、任务或消息消费时必须确保执行结束后清理。
 * 对于可能嵌套的入口，优先使用 {@link #scope(AuthenticatedUser)} 和 try-with-resources，确保外层安全上下文
 * 与操作上下文在正常、异常路径都能恢复。</p>
 */
public final class SecurityContext {

    private static final ThreadLocal<SecurityBinding> CURRENT = new ThreadLocal<>();

    private SecurityContext() {
    }

    /**
     * 设置当前已认证用户，并同步建立 OperationContext。
     *
     * <p>连续调用 set 表示替换当前安全主体。最后调用 {@link #clear()} 时，会恢复第一次建立安全上下文前的
     * OperationContext，而不会恢复被替换的旧用户。需要嵌套恢复旧用户时应使用 {@link #scope(AuthenticatedUser)}。</p>
     *
     * @param authenticatedUser 已认证用户；传入 null 时清理上下文
     */
    public static void set(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            clear();
            return;
        }

        OperationContext operationContext = SecurityOperationContextAdapter.toOperationContext(authenticatedUser);
        SecurityBinding current = CURRENT.get();
        OperationContextSnapshot previousOperationContext = current == null
                ? OperationContextHolder.snapshot()
                : current.previousOperationContext();

        CURRENT.set(new SecurityBinding(authenticatedUser, previousOperationContext));
        OperationContextHolder.set(operationContext);
    }

    /**
     * 建立可嵌套的安全上下文作用域。
     *
     * <p>作用域关闭时会精确恢复进入作用域前的 AuthenticatedUser 和 OperationContext。传入 null 表示在该作用域内
     * 临时清除安全主体；这适合 trusted-header fail-open 场景，避免错误沿用外层认证用户。</p>
     *
     * @param authenticatedUser 作用域内用户；允许为 null
     * @return 可关闭的安全上下文作用域
     */
    public static SecurityContextScope scope(AuthenticatedUser authenticatedUser) {
        SecurityBinding previousBinding = CURRENT.get();
        OperationContextSnapshot previousOperationContext = OperationContextHolder.snapshot();
        set(authenticatedUser);
        return new SecurityContextScope(() -> restore(previousBinding, previousOperationContext));
    }

    /**
     * 返回当前已认证用户。
     */
    public static Optional<AuthenticatedUser> currentUser() {
        SecurityBinding binding = CURRENT.get();
        return binding == null ? Optional.empty() : Optional.of(binding.authenticatedUser());
    }

    /**
     * 清理当前安全上下文，并恢复首次建立当前安全绑定前的 OperationContext。
     *
     * <p>重复清理是安全的；当前线程没有安全绑定时，不会修改独立存在的 Job、Async 或 MQ OperationContext。</p>
     */
    public static void clear() {
        SecurityBinding binding = CURRENT.get();
        CURRENT.remove();
        if (binding != null) {
            OperationContextHolder.set(binding.previousOperationContext().context());
        }
    }

    /**
     * 当当前没有认证用户时执行防御性清理。
     *
     * <p>该方法不会清理独立存在的 OperationContext。正常生命周期应优先使用 {@link #scope(AuthenticatedUser)}
     * 或直接调用 {@link #clear()}。</p>
     */
    public static void clearIfEmpty() {
        if (CURRENT.get() == null) {
            CURRENT.remove();
        }
    }

    private static void restore(
            SecurityBinding previousBinding,
            OperationContextSnapshot previousOperationContext) {
        if (previousBinding == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previousBinding);
        }
        OperationContextHolder.set(previousOperationContext.context());
    }

    private record SecurityBinding(
            AuthenticatedUser authenticatedUser,
            OperationContextSnapshot previousOperationContext) {
    }
}
