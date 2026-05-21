package com.indigo.synapse.common.id;

import java.util.UUID;

public final class UuidIdGenerator implements IdGenerator {

    public static final UuidIdGenerator INSTANCE = new UuidIdGenerator();

    private UuidIdGenerator() {
    }

    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
