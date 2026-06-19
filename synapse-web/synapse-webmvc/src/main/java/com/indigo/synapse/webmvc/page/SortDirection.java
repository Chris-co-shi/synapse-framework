package com.indigo.synapse.webmvc.page;

import java.util.Locale;

public enum SortDirection {

    ASC,
    DESC;

    public static SortDirection from(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }
        return SortDirection.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
