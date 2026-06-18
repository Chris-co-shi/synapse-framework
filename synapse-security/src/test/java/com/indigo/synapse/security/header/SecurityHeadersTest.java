package com.indigo.synapse.security.header;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityHeadersTest {

    @Test
    void shouldNotExposeTrustedHeaderContract() {
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("com.indigo.synapse.security.header.SecurityHeaders")
        );
    }
}
