package com.indigo.synapse.datapermission.resolver;

import com.indigo.synapse.datapermission.model.DataPermissionScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataPermissionResolverTest {

    @Test
    void shouldReturnAllScopeByDefault() {
        DataPermissionResolver resolver = (subjectId, permissionKey) -> com.indigo.synapse.datapermission.model.DataPermissionPolicy.all();

        assertEquals(DataPermissionScope.ALL, resolver.resolve("1", "user:list").scope());
    }
}
