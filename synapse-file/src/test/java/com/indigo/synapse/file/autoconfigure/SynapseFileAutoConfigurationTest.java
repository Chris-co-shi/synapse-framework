package com.indigo.synapse.file.autoconfigure;

import com.indigo.synapse.file.storage.FileStorage;
import com.indigo.synapse.file.storage.local.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SynapseFileAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseFileAutoConfiguration.class));

    @Test
    void shouldCreateLocalFileStorageByDefault() {
        contextRunner.run(context -> assertInstanceOf(LocalFileStorage.class, context.getBean(FileStorage.class)));
    }
}
