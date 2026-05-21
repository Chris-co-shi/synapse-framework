package com.indigo.synapse.data.fill;

import java.util.Optional;

@FunctionalInterface
public interface SynapseAuditorProvider {

    Optional<String> currentAuditor();

    static SynapseAuditorProvider empty() {
        return Optional::empty;
    }
}
