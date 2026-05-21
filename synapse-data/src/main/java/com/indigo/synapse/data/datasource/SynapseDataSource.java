package com.indigo.synapse.data.datasource;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DS("")
public @interface SynapseDataSource {

    @AliasFor(annotation = DS.class, attribute = "value")
    String value();
}
