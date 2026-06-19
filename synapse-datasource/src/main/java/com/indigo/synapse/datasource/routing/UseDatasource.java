package com.indigo.synapse.datasource.routing;

import java.lang.annotation.*;

/**
 * 声明方法执行前应选择的数据源 key；优先级低于外层显式 Scope。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseDatasource {

    /**
     * @return dynamic-datasource 中已注册的数据源 key
     */
    String value();
}
