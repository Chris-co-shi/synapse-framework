package com.indigo.synapse.tenant.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantIgnoreTest {

    @Test
    void shouldExposeTenantIgnoreAtRuntime() throws NoSuchMethodException {
        Method method = Sample.class.getDeclaredMethod("list");

        assertTrue(Sample.class.isAnnotationPresent(TenantIgnore.class));
        assertTrue(method.isAnnotationPresent(TenantIgnore.class));
    }

    @TenantIgnore
    static final class Sample {

        @TenantIgnore
        void list() {
        }
    }
}
