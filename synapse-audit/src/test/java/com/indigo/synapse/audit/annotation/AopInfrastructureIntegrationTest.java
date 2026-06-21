package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.autoconfigure.SynapseAuditAutoConfiguration;
import com.indigo.synapse.audit.publish.AuditPublisher;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityAutoConfiguration;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.permission.PermissionChecker;
import com.indigo.synapse.security.permission.RequirePermission;
import com.indigo.synapse.security.permission.RequirePermissionAspect;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class AopInfrastructureIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AopAutoConfiguration.class,
                    SynapseSecurityAutoConfiguration.class,
                    SynapseAuditAutoConfiguration.class))
            .withPropertyValues("spring.aop.proxy-target-class=false")
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldUseOneAutoProxyCreatorAndApplyAllAdvisors(CapturedOutput output) {
        contextRunner.run(context -> {
            assertThat(context.getBeansOfType(AbstractAutoProxyCreator.class)).hasSize(1);

            InvocationEvents events = context.getBean(InvocationEvents.class);
            ContractService interfaceService = context.getBean(ContractService.class);
            ConcreteService concreteService = context.getBean(ConcreteService.class);

            assertThat(Proxy.isProxyClass(interfaceService.getClass())).isTrue();
            assertThat(AopUtils.isCglibProxy(concreteService)).isTrue();
            assertThat(((Advised) interfaceService).getAdvisors()).hasSize(4);

            assertThat(interfaceService.invoke()).isEqualTo("interface");
            assertThat(events.snapshot()).containsExactly(
                    "security", "custom", "transaction.begin", "business.interface",
                    "audit.active=true", "transaction.commit"
            );

            events.clear();
            assertThat(concreteService.invoke()).isEqualTo("concrete");
            assertThat(events.snapshot()).containsExactly(
                    "security", "custom", "transaction.begin", "business.concrete",
                    "audit.active=true", "transaction.commit"
            );

            RequirePermissionAspect securityAdvisor = context.getBean(RequirePermissionAspect.class);
            AuditMethodAdvisor auditAdvisor = context.getBean(AuditMethodAdvisor.class);
            Advisor transactionAdvisor = context.getBean(
                    "org.springframework.transaction.config.internalTransactionAdvisor",
                    Advisor.class
            );
            assertThat(securityAdvisor.getOrder()).isEqualTo(RequirePermissionAspect.ORDER);
            assertThat(transactionAdvisor).isInstanceOf(org.springframework.core.Ordered.class);
            assertThat(((org.springframework.core.Ordered) transactionAdvisor).getOrder()).isZero();
            assertThat(auditAdvisor.getOrder()).isEqualTo(AuditMethodAdvisor.ORDER);
        });

        assertThat(output).doesNotContain("not eligible for getting processed by all BeanPostProcessors");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(order = 0, proxyTargetClass = false)
    static class TestConfiguration {

        @Bean
        InvocationEvents invocationEvents() {
            return new InvocationEvents();
        }

        @Bean
        PermissionChecker permissionChecker(InvocationEvents events) {
            return new RecordingPermissionChecker(events);
        }

        @Bean
        @Primary
        AuditPublisher auditPublisher(InvocationEvents events, RecordingTransactionManager transactionManager) {
            return (event, policy) -> events.add("audit.active=" + transactionManager.isTransactionActive());
        }

        @Bean
        RecordingTransactionManager transactionManager(InvocationEvents events) {
            return new RecordingTransactionManager(events);
        }

        @Bean
        Advisor customAdvisor(InvocationEvents events) {
            StaticMethodMatcherPointcut serviceMethods = new StaticMethodMatcherPointcut() {
                @Override
                public boolean matches(Method method, Class<?> targetClass) {
                    return method.getName().equals("invoke");
                }
            };
            DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(serviceMethods, (MethodInterceptor) invocation -> {
                events.add("custom");
                return invocation.proceed();
            });
            advisor.setOrder(-100);
            return advisor;
        }

        @Bean
        ContractService contractService(InvocationEvents events) {
            return new ContractServiceImpl(events);
        }

        @Bean
        ConcreteService concreteService(InvocationEvents events) {
            return new ConcreteService(events);
        }
    }

    interface ContractService {
        String invoke();
    }

    static final class ContractServiceImpl implements ContractService {
        private final InvocationEvents events;

        ContractServiceImpl(InvocationEvents events) {
            this.events = events;
        }

        @Override
        @Transactional
        @RequirePermission("demo:read")
        @Audited(action = "interface.invoke", targetType = "TEST")
        public String invoke() {
            events.add("business.interface");
            return "interface";
        }
    }

    static class ConcreteService {
        private final InvocationEvents events;

        ConcreteService(InvocationEvents events) {
            this.events = events;
        }

        @Transactional
        @RequirePermission("demo:read")
        @Audited(action = "concrete.invoke", targetType = "TEST")
        public String invoke() {
            events.add("business.concrete");
            return "concrete";
        }
    }

    static final class InvocationEvents {
        private final List<String> values = new ArrayList<>();

        void add(String value) {
            values.add(value);
        }

        List<String> snapshot() {
            return List.copyOf(values);
        }

        void clear() {
            values.clear();
        }
    }

    static final class RecordingPermissionChecker implements PermissionChecker {
        private final InvocationEvents events;

        RecordingPermissionChecker(InvocationEvents events) {
            this.events = events;
        }

        @Override
        public void require(String permission) {
            events.add("security");
        }

        @Override
        public boolean has(String permission) {
            return true;
        }

        @Override
        public AuthenticatedUser requireUser() {
            throw new UnsupportedOperationException("not used");
        }
    }

    static final class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        private final InvocationEvents events;
        private final ThreadLocal<Boolean> active = ThreadLocal.withInitial(() -> false);

        RecordingTransactionManager(InvocationEvents events) {
            this.events = events;
        }

        boolean isTransactionActive() {
            return active.get();
        }

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            active.set(true);
            events.add("transaction.begin");
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            events.add("transaction.commit");
            active.remove();
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            events.add("transaction.rollback");
            active.remove();
        }
    }
}
