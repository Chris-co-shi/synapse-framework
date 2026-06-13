package com.indigo.synapse.datapermission.context;

import com.indigo.synapse.datapermission.model.DataPermissionPolicy;
import com.indigo.synapse.datapermission.model.DataPermissionScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataPermissionContextTest {

    @AfterEach
    void tearDown() {
        DataPermissionContext.clear();
    }

    @Test
    void shouldStoreAndRestoreContext() {
        DataPermissionContextSnapshot snapshot = new DataPermissionContextSnapshot("1", "tenant-a", new DataPermissionPolicy(DataPermissionScope.SELF, Set.of()));
        DataPermissionContext.set(snapshot);

        assertEquals(snapshot, DataPermissionContext.current().orElseThrow());

        try (DataPermissionContextScope ignored = DataPermissionContext.scope(new DataPermissionContextSnapshot("2", "tenant-a", DataPermissionPolicy.all()))) {
            assertEquals("2", DataPermissionContext.current().orElseThrow().subjectId());
        }

        assertEquals("1", DataPermissionContext.current().orElseThrow().subjectId());
    }

    @Test
    void shouldClearNullSnapshot() {
        DataPermissionContext.set(null);
        assertTrue(DataPermissionContext.current().isEmpty());
    }
}
