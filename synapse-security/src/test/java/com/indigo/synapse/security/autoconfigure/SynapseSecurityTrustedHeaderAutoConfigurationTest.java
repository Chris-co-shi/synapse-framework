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
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseSecurityTrustedHeaderAutoConfigurationTest {

    @Test
    void shouldKeepTrustedHeaderDisabledByDefault() {
        SynapseSecurityProperties properties = new SynapseSecurityProperties();

        assertTrue(!properties.getTrustedHeader().isEnabled());
        assertTrue(properties.getTrustedHeader().isSignatureEnabled());
        assertTrue(properties.getTrustedHeader().isFailFast());
        assertTrue(properties.getPermission().isAnnotationEnabled());
        assertEquals(Duration.ofSeconds(300), properties.getTrustedHeader().getTimestampTolerance());
    }

    @Test
    void shouldValidateSecretWhenSignatureEnabled() {
        SynapseSecurityProperties properties = new SynapseSecurityProperties();
        properties.getTrustedHeader().setEnabled(true);
        properties.getTrustedHeader().setSignatureEnabled(true);

        assertThrows(IllegalStateException.class, properties::validateTrustedHeaderConfiguration);

        properties.getTrustedHeader().setSecret("secret-value");
        assertDoesNotThrow(properties::validateTrustedHeaderConfiguration);
    }

    @Test
    void shouldAllowMissingSecretWhenSignatureDisabled() {
        SynapseSecurityProperties properties = new SynapseSecurityProperties();
        properties.getTrustedHeader().setEnabled(true);
        properties.getTrustedHeader().setSignatureEnabled(false);

        assertDoesNotThrow(properties::validateTrustedHeaderConfiguration);
    }

    @Test
    void shouldCreateTrustedHeaderProtocolComponents() {
        SynapseSecurityAutoConfiguration autoConfiguration = new SynapseSecurityAutoConfiguration();

        assertNotNull(autoConfiguration.trustedHeaderAuthenticatedUserResolver());
        assertNotNull(autoConfiguration.trustedHeaderSignatureVerifier());
        assertNotNull(autoConfiguration.trustedHeaderTimestampValidator());
    }

    @Test
    void shouldCreateDefaultPermissionChecker() {
        SynapseSecurityAutoConfiguration autoConfiguration = new SynapseSecurityAutoConfiguration();

        PermissionChecker permissionChecker = autoConfiguration.permissionChecker();

        assertTrue(permissionChecker instanceof DefaultPermissionChecker);
    }

    @Test
    void shouldNotOverrideCustomPermissionChecker() throws NoSuchMethodException {
        Method permissionCheckerMethod = SynapseSecurityAutoConfiguration.class.getDeclaredMethod("permissionChecker");

        ConditionalOnMissingBean conditional = permissionCheckerMethod.getAnnotation(ConditionalOnMissingBean.class);

        assertNotNull(conditional);
        assertEquals(PermissionChecker.class, conditional.value()[0]);
    }

//    @Test
//    void shouldCreateRequirePermissionAspect() {
//        SynapseSecurityAutoConfiguration autoConfiguration = new SynapseSecurityAutoConfiguration();
//        PermissionChecker permissionChecker = autoConfiguration.permissionChecker();
//
//        RequirePermissionAspect aspect = autoConfiguration.requirePermissionAspect(permissionChecker);
//
//        assertNotNull(aspect);
//    }

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

    @Test
    void shouldNotDeclareServletFilterInSecurityAutoConfiguration() {
        assertThrows(NoSuchMethodException.class, () -> SynapseSecurityAutoConfiguration.class.getDeclaredMethod(
                "trustedHeaderAuthenticationFilter",
                SynapseSecurityProperties.class
        ));
    }
}
