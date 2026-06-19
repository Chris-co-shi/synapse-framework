package com.indigo.synapse.webmvc.page;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageRequestTest {

    @Test
    void shouldUseDefaultsWhenValuesAreMissing() {
        PageRequest request = PageRequest.of(null, null);

        assertEquals(1, request.getPageNo());
        assertEquals(20, request.getPageSize());
        assertEquals(0, request.offset());
    }

    @Test
    void shouldNormalizeInvalidValues() {
        PageRequest request = PageRequest.of(0, 0);

        assertEquals(1, request.getPageNo());
        assertEquals(20, request.getPageSize());
    }

    @Test
    void shouldLimitMaxPageSize() {
        PageRequest request = PageRequest.of(3, 500);

        assertEquals(3, request.getPageNo());
        assertEquals(200, request.getPageSize());
        assertEquals(400, request.offset());
    }
}
