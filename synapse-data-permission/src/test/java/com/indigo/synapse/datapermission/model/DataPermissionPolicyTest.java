package com.indigo.synapse.datapermission.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataPermissionPolicyTest {

    @Test
    void shouldValidatePolicyRules() {
        assertEquals(DataPermissionScope.ALL, DataPermissionPolicy.all().scope());
        assertThrows(IllegalArgumentException.class, () -> new DataPermissionPolicy(null, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new DataPermissionPolicy(DataPermissionScope.CUSTOM_DEPT, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new DataPermissionPolicy(DataPermissionScope.ALL, Set.of("1")));
    }
}
