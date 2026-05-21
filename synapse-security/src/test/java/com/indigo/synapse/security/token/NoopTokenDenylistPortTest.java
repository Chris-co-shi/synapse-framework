package com.indigo.synapse.security.token;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoopTokenDenylistPortTest {

    @Test
    void shouldRejectBlankTokenIdAndIgnoreDenyState() {
        NoopTokenDenylistPort port = new NoopTokenDenylistPort();

        assertThrows(IllegalArgumentException.class, () -> port.deny("", Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> port.isDenied(" "));
        assertFalse(port.isDenied("token-1"));
    }
}
