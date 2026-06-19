package com.indigo.synapse.data.audit;

import java.util.Optional;

public interface DataAuditorProvider {

    Optional<String> currentAuditor();

    static DataAuditorProvider empty() {
        return Optional::empty;
    }
}
