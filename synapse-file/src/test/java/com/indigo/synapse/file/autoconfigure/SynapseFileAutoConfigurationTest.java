package com.indigo.synapse.file.autoconfigure;

import com.indigo.synapse.file.storage.FileStorage;
import com.indigo.synapse.file.storage.StoreFileCommand;
import com.indigo.synapse.file.storage.FileObject;
import com.indigo.synapse.file.storage.StoredFile;
import com.indigo.synapse.file.storage.local.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class SynapseFileAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseFileAutoConfiguration.class));

    @Test
    void shouldCreateLocalFileStorageByDefault() {
        contextRunner.run(context -> assertInstanceOf(LocalFileStorage.class, context.getBean(FileStorage.class)));
    }

    @Test
    void shouldNotOverrideCustomFileStorage() {
        FileStorage custom = new NoopFileStorage();

        contextRunner
                .withBean(FileStorage.class, () -> custom)
                .run(context -> assertSame(custom, context.getBean(FileStorage.class)));
    }

    private static final class NoopFileStorage implements FileStorage {

        @Override
        public FileObject store(StoreFileCommand command) {
            throw new UnsupportedOperationException("not required for auto configuration test");
        }

        @Override
        public Optional<StoredFile> load(String bucket, String objectKey) {
            return Optional.empty();
        }

        @Override
        public boolean delete(String bucket, String objectKey) {
            return false;
        }
    }
}
