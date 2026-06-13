package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.event.DomainEvent;
import com.indigo.synapse.message.publisher.DomainEventPublisher;

public final class NoopDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
    }
}
