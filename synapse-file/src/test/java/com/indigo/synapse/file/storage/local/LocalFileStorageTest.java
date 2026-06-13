package com.indigo.synapse.file.storage.local;

import com.indigo.synapse.file.storage.StoreFileCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStoreLoadAndDeleteFile() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(tempDir);

        var metadata = storage.store(new StoreFileCommand(
                "private",
                "docs/readme.txt",
                "text/plain",
                new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8))
        ));

        assertEquals(5, metadata.size());
        var stored = storage.load("private", "docs/readme.txt").orElseThrow();
        assertEquals("hello", new String(stored.content().readAllBytes(), StandardCharsets.UTF_8));
        assertTrue(storage.delete("private", "docs/readme.txt"));
        assertFalse(storage.load("private", "docs/readme.txt").isPresent());
    }

    @Test
    void shouldRejectPathTraversal() {
        LocalFileStorage storage = new LocalFileStorage(tempDir);

        assertThrows(IllegalArgumentException.class, () -> storage.load("private", "../secret.txt"));
        assertThrows(IllegalArgumentException.class, () -> storage.load("../private", "a.txt"));
    }
}
