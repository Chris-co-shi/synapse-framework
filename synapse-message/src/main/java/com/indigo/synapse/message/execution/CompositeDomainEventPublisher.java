package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.event.DomainEvent;
import com.indigo.synapse.message.publisher.DomainEventPublisher;

import java.util.List;

public final class CompositeDomainEventPublisher implements DomainEventPublisher {

    private final List<DomainEventPublisher> delegates;

    public CompositeDomainEventPublisher(List<DomainEventPublisher> delegates) {
        if (delegates == null || delegates.isEmpty()) {
            throw new IllegalArgumentException("delegates must not be empty");
        }
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        for (DomainEventPublisher delegate : delegates) {
            delegate.publish(event);
        }
    }
}
