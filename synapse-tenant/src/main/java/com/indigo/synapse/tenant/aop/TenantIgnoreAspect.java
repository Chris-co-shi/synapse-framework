package com.indigo.synapse.tenant.aop;

import com.indigo.synapse.tenant.annotation.TenantIgnore;
import com.indigo.synapse.tenant.ignore.TenantIgnoreScope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TenantIgnoreAspect {

    @Around("@within(com.indigo.synapse.tenant.annotation.TenantIgnore) || @annotation(com.indigo.synapse.tenant.annotation.TenantIgnore)")
    public Object aroundTenantIgnore(ProceedingJoinPoint joinPoint) throws Throwable {
        try (TenantIgnoreScope ignored = TenantIgnoreScope.open()) {
            return joinPoint.proceed();
        }
    }
}
