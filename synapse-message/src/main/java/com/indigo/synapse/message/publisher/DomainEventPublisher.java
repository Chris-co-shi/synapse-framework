package com.indigo.synapse.message.publisher;

import com.indigo.synapse.message.event.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
