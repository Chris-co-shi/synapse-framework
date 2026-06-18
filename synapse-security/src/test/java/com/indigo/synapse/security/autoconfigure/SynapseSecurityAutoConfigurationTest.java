package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.permission.DefaultPermissionChecker;
import com.indigo.synapse.security.permission.PermissionChecker;
import com.indigo.synapse.security.permission.RequirePermissionAspect;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseSecurityAutoConfigurationTest {

    @Test
    void shouldEnablePermissionAnnotationByDefault() {
        SynapseSecurityProperties properties = new SynapseSecurityProperties();

        assertTrue(properties.getPermission().isAnnotationEnabled());
    }

    @Test
    void shouldCreateDefaultSecurityBeans() {
        SynapseSecurityAutoConfiguration autoConfiguration = new SynapseSecurityAutoConfiguration();

        assertNotNull(autoConfiguration.synapsePasswordEncoder());
        assertTrue(autoConfiguration.permissionChecker() instanceof DefaultPermissionChecker);
    }

    @Test
    void shouldNotOverrideCustomPermissionChecker() throws NoSuchMethodException {
        Method permissionCheckerMethod = SynapseSecurityAutoConfiguration.class.getDeclaredMethod("permissionChecker");

        ConditionalOnMissingBean conditional = permissionCheckerMethod.getAnnotation(ConditionalOnMissingBean.class);

        assertNotNull(conditional);
        assertEquals(PermissionChecker.class, conditional.value()[0]);
    }

    @Test
    void shouldDeclareRequirePermissionAspectConditions() throws NoSuchMethodException {
        Method method = SynapseSecurityAutoConfiguration.class.getDeclaredMethod(
                "requirePermissionAspect",
                ObjectProvider.class
        );

        ConditionalOnClass onClass = method.getAnnotation(ConditionalOnClass.class);
        ConditionalOnBean onBean = method.getAnnotation(ConditionalOnBean.class);
        ConditionalOnMissingBean missingBean = method.getAnnotation(ConditionalOnMissingBean.class);
        ConditionalOnProperty property = method.getAnnotation(ConditionalOnProperty.class);

        assertNotNull(onClass);
        assertEquals(MethodInterceptor.class, onClass.value()[0]);
        assertNotNull(onBean);
        assertEquals(PermissionChecker.class, onBean.value()[0]);
        assertNotNull(missingBean);
        assertEquals(RequirePermissionAspect.class, missingBean.value()[0]);
        assertNotNull(property);
        assertEquals("synapse.security.permission", property.prefix());
        assertEquals("annotation-enabled", property.name()[0]);
        assertEquals("true", property.havingValue());
        assertTrue(property.matchIfMissing());
    }
}
