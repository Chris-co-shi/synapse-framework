package com.indigo.synapse.data.autoconfigure;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseIdentifierGeneratorTest {

    @Test
    void shouldGenerateStringIdWithinVarchar19Limit() {
        IdentifierGenerator identifierGenerator = new SynapseDataAutoConfiguration().synapseIdentifierGenerator();

        String id = identifierGenerator.nextUUID(new Object());

        assertThat(id).hasSizeLessThanOrEqualTo(19);
        assertThat(id).containsOnlyDigits();
    }

    @Test
    void shouldGenerateNumericIdWithinVarchar19Limit() {
        IdentifierGenerator identifierGenerator = new SynapseDataAutoConfiguration().synapseIdentifierGenerator();

        String id = identifierGenerator.nextId(new Object()).toString();

        assertThat(id).hasSizeLessThanOrEqualTo(19);
        assertThat(id).containsOnlyDigits();
    }
}
