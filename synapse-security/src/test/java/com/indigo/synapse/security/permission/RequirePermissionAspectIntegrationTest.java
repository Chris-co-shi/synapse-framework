package com.indigo.synapse.security.permission;

import com.indigo.synapse.security.autoconfigure.SynapseSecurityAutoConfiguration;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirePermissionAspectIntegrationTest {

    @Test
    void shouldInvokePermissionCheckerForMethodLevelAnnotationInSpringContext() {
        try (AnnotationConfigApplicationContext context = context(TestConfiguration.class)) {
            RecordingPermissionChecker permissionChecker = context.getBean(RecordingPermissionChecker.class);
            SecuredService service = context.getBean(SecuredService.class);

            String result = service.read();

            assertTrue(service instanceof Advised);
            assertEquals("ok", result);
            assertEquals(List.of("demo:read"), permissionChecker.required);
            assertEquals(1, service.readExecuted);
        }
    }

    @Test
    void shouldNotProceedWhenPermissionCheckerThrowsInSpringContext() {
        try (AnnotationConfigApplicationContext context = context(TestConfiguration.class)) {
            RecordingPermissionChecker permissionChecker = context.getBean(RecordingPermissionChecker.class);
            permissionChecker.throwAccessDenied = true;
            SecuredService service = context.getBean(SecuredService.class);

            assertThrows(SynapseAccessDeniedException.class, service::read);

            assertEquals(List.of("demo:read"), permissionChecker.required);
            assertEquals(0, service.readExecuted);
        }
    }

    @Test
    void shouldNotInvokePermissionCheckerForUnannotatedMethodInSpringContext() {
        try (AnnotationConfigApplicationContext context = context(TestConfiguration.class)) {
            RecordingPermissionChecker permissionChecker = context.getBean(RecordingPermissionChecker.class);
            SecuredService service = context.getBean(SecuredService.class);

            String result = service.open();

            assertEquals("open", result);
            assertEquals(List.of(), permissionChecker.required);
            assertEquals(1, service.openExecuted);
        }
    }

    @Test
    void shouldNotRegisterAutoProxyCreatorWhenAnnotationDisabled() {
        try (AnnotationConfigApplicationContext context = context(
                TestConfiguration.class,
                Map.of("synapse.security.permission.annotation-enabled", "false")
        )) {
            assertThrows(NoSuchBeanDefinitionException.class, () ->
                    context.getBean(DefaultAdvisorAutoProxyCreator.class));
        }
    }

    @Test
    void shouldNotOverrideExistingAutoProxyCreator() {
        try (AnnotationConfigApplicationContext context = context(CustomAutoProxyCreatorConfiguration.class)) {
            assertEquals(1, context.getBeansOfType(DefaultAdvisorAutoProxyCreator.class).size());
            assertEquals("customAutoProxyCreator",
                    context.getBeanNamesForType(DefaultAdvisorAutoProxyCreator.class)[0]);
        }
    }

    private static AnnotationConfigApplicationContext context(Class<?> configurationClass) {
        return context(configurationClass, Map.of());
    }

    private static AnnotationConfigApplicationContext context(Class<?> configurationClass, Map<String, Object> properties) {
        SecuredService.reset();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
        context.register(configurationClass, SynapseSecurityAutoConfiguration.class);
        context.refresh();
        return context;
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        RecordingPermissionChecker permissionChecker() {
            return new RecordingPermissionChecker();
        }

        @Bean
        SecuredService securedService() {
            return new SecuredService();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomAutoProxyCreatorConfiguration extends TestConfiguration {

        @Bean
        static DefaultAdvisorAutoProxyCreator customAutoProxyCreator() {
            return new DefaultAdvisorAutoProxyCreator();
        }
    }

    static final class RecordingPermissionChecker implements PermissionChecker {

        private final List<String> required = new ArrayList<>();
        private boolean throwAccessDenied;

        @Override
        public void require(String permission) {
            required.add(permission);
            if (throwAccessDenied) {
                throw new SynapseAccessDeniedException();
            }
        }

        @Override
        public boolean has(String permission) {
            return false;
        }

        @Override
        public com.indigo.synapse.security.context.AuthenticatedUser requireUser() {
            throw new UnsupportedOperationException("not used");
        }
    }

    static class SecuredService {

        private static int readExecuted;
        private static int openExecuted;

        @RequirePermission("demo:read")
        public String read() {
            readExecuted++;
            return "ok";
        }

        public String open() {
            openExecuted++;
            return "open";
        }

        private static void reset() {
            readExecuted = 0;
            openExecuted = 0;
        }
    }
}
