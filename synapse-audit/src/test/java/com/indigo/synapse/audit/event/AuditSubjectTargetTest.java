package com.indigo.synapse.audit.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditSubjectTargetTest {

    @Test
    void shouldRejectInvalidSubject() {
        assertThrows(IllegalArgumentException.class, () -> new AuditSubject("", "1", null));
        assertThrows(IllegalArgumentException.class, () -> new AuditSubject("USER", "", null));
    }

    @Test
    void shouldRejectInvalidTarget() {
        assertThrows(IllegalArgumentException.class, () -> new AuditTarget("", "1"));
        assertThrows(IllegalArgumentException.class, () -> new AuditTarget("USER", ""));
    }
}
