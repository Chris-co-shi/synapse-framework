package com.indigo.synapse.mybatisplus.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.springframework.core.Ordered;

import java.util.Optional;

/**
 * MyBatis-Plus 内部插件贡献接口。
 *
 * <p>消费方可以通过实现该接口向 Synapse 默认的 {@code MybatisPlusInterceptor} 追加插件，
 * 无需为了增加一个插件而整体替换默认插件链。贡献者按 {@link Ordered#getOrder()} 升序执行。</p>
 *
 * <p>该扩展点只负责 MyBatis-Plus 插件装配，不负责 SQL 自动读写路由、DataSource 切换或业务 Mapper
 * 行为。实现应保持无状态或自行保证线程安全。</p>
 */
public interface SynapseInnerInterceptorContributor extends Ordered {

    /**
     * 贡献一个 MyBatis-Plus InnerInterceptor。
     *
     * @return 需要追加到插件链的拦截器；empty 表示当前环境不贡献插件
     */
    Optional<InnerInterceptor> contribute();

    @Override
    default int getOrder() {
        return 0;
    }
}
