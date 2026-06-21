package com.indigo.synapse.audit.autoconfigure;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.transaction.config.TransactionManagementConfigUtils;

public final class AuditTransactionInfrastructureValidator implements SmartInitializingSingleton {
    private final ConfigurableListableBeanFactory beanFactory;

    public AuditTransactionInfrastructureValidator(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
        if (!beanFactory.containsBean(name)) {
            throw new IllegalStateException("Audit transactional mode requires Spring transaction management");
        }
        Advisor advisor = beanFactory.getBean(name, Advisor.class);
        if (!(advisor instanceof Ordered ordered) || ordered.getOrder() != 0) {
            throw new IllegalStateException("Audit transactional mode requires transaction Advisor order 0");
        }
    }
}
