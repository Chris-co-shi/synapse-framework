package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.event.DomainEvent;
import com.indigo.synapse.message.publisher.DomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventPublisherTest {

    @Test
    void noopPublisherShouldValidateEvent() {
        NoopDomainEventPublisher publisher = new NoopDomainEventPublisher();

        publisher.publish(event());

        assertThrows(IllegalArgumentException.class, () -> publisher.publish(null));
    }

    @Test
    void compositePublisherShouldDispatchToAllDelegates() {
        List<DomainEvent> first = new ArrayList<>();
        List<DomainEvent> second = new ArrayList<>();
        CompositeDomainEventPublisher publisher = new CompositeDomainEventPublisher(List.of(first::add, second::add));
        DomainEvent event = event();

        publisher.publish(event);

        assertEquals(List.of(event), first);
        assertEquals(List.of(event), second);
    }

    @Test
    void compositePublisherShouldRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeDomainEventPublisher(List.of()));
        DomainEventPublisher publisher = new CompositeDomainEventPublisher(List.of(event -> {
        }));
        assertThrows(IllegalArgumentException.class, () -> publisher.publish(null));
    }

    private static DomainEvent event() {
        return new DomainEvent(
                "event-1",
                "iam.user.created",
                "USER",
                "1001",
                Instant.parse("2026-05-21T10:00:00Z"),
                "trace-1",
                Map.of(),
                "{}"
        );
    }
}
