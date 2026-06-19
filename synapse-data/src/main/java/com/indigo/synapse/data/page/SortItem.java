package com.indigo.synapse.data.page;

import java.io.Serializable;
import java.util.Objects;

public record SortItem(
        String field,
        SortDirection direction
) implements Serializable {

    public SortItem {
        field = Objects.requireNonNull(field, "field must not be null");
        direction = direction == null ? SortDirection.ASC : direction;
    }

    public static SortItem asc(String field) {
        return new SortItem(field, SortDirection.ASC);
    }

    public static SortItem desc(String field) {
        return new SortItem(field, SortDirection.DESC);
    }
}
