package com.indigo.synapse.audit.autoconfigure;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.transaction.config.TransactionManagementConfigUtils;

/** 将 Spring 标准事务 Advisor 固定在 Security 与 Audit 之间。 */
public final class AuditTransactionAdvisorOrderPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {
    public static final int TRANSACTION_ADVISOR_ORDER = 0;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        String beanName = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
        if (!beanFactory.containsBeanDefinition(beanName)) return;
        BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
        definition.getPropertyValues().add("order", TRANSACTION_ADVISOR_ORDER);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
