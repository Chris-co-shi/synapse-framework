package com.indigo.synapse.data.autoconfigure;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

public final class SynapseIdentifierGenerator implements IdentifierGenerator {

    private final DefaultIdentifierGenerator delegate = DefaultIdentifierGenerator.getInstance();

    @Override
    public Number nextId(Object entity) {
        return delegate.nextId(entity);
    }

    @Override
    public String nextUUID(Object entity) {
        return nextId(entity).toString();
    }
}
