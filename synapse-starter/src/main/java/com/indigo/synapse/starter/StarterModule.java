package com.indigo.synapse.starter;

import com.indigo.synapse.audit.AuditModule;
import com.indigo.synapse.common.CommonModule;
import com.indigo.synapse.data.DataModule;
import com.indigo.synapse.security.SecurityModule;
import com.indigo.synapse.web.WebModule;

import java.util.List;

public final class StarterModule {

    public static final String NAME = "synapse-starter";

    private StarterModule() {
    }

    public static List<String> modules() {
        return List.of(
                CommonModule.NAME,
                WebModule.NAME,
                DataModule.NAME,
                SecurityModule.NAME,
                AuditModule.NAME
        );
    }
}
