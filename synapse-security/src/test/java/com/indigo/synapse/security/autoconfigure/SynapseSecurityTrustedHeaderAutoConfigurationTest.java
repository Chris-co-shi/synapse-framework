package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.web.TrustedHeaderAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

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
    void shouldCreateFilterAndRegistrationWhenBeanMethodsAreInvoked() {
        SynapseSecurityProperties properties = new SynapseSecurityProperties();
        properties.getTrustedHeader().setEnabled(true);
        properties.getTrustedHeader().setSecret("secret-value");
        SynapseSecurityAutoConfiguration autoConfiguration = new SynapseSecurityAutoConfiguration();

        TrustedHeaderAuthenticationFilter filter = autoConfiguration.trustedHeaderAuthenticationFilter(properties);
        FilterRegistrationBean<TrustedHeaderAuthenticationFilter> registration =
                autoConfiguration.trustedHeaderAuthenticationFilterRegistration(filter);

        assertNotNull(filter);
        assertEquals("trustedHeaderAuthenticationFilter", registration.getFilterName());
        assertEquals(-100, registration.getOrder());
    }

    @Test
    void shouldDeclareServletAndPropertyConditions() throws NoSuchMethodException {
        Method filterMethod = SynapseSecurityAutoConfiguration.class.getDeclaredMethod(
                "trustedHeaderAuthenticationFilter",
                SynapseSecurityProperties.class
        );
        Method registrationMethod = SynapseSecurityAutoConfiguration.class.getDeclaredMethod(
                "trustedHeaderAuthenticationFilterRegistration",
                TrustedHeaderAuthenticationFilter.class
        );

        assertServletEnabledConditional(filterMethod);
        assertServletEnabledConditional(registrationMethod);
        assertNotNull(filterMethod.getAnnotation(ConditionalOnMissingBean.class));
    }

    private static void assertServletEnabledConditional(Method method) {
        ConditionalOnWebApplication webApplication = method.getAnnotation(ConditionalOnWebApplication.class);
        ConditionalOnProperty property = method.getAnnotation(ConditionalOnProperty.class);

        assertNotNull(webApplication);
        assertEquals(ConditionalOnWebApplication.Type.SERVLET, webApplication.type());
        assertNotNull(property);
        assertEquals("synapse.security.trusted-header", property.prefix());
        assertEquals("enabled", property.name()[0]);
        assertEquals("true", property.havingValue());
    }
}
