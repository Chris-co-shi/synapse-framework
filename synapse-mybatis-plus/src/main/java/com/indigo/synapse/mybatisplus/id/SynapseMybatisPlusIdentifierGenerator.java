package com.indigo.synapse.mybatisplus.id;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

public final class SynapseMybatisPlusIdentifierGenerator implements IdentifierGenerator {

    private final DefaultIdentifierGenerator delegate = DefaultIdentifierGenerator.getInstance();

    @Override
    public Number nextId(Object entity) {
        return delegate.nextId(entity);
    }

    @Override
    public String nextUUID(Object entity) {
        return delegate.nextUUID(entity);
    }
}
