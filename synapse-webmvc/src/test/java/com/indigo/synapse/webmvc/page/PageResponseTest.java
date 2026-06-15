package com.indigo.synapse.webmvc.page;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageResponseTest {

    @Test
    void shouldKeepPageMetadataAndCopyRecords() {
        List<String> records = new ArrayList<>();
        records.add("a");

        PageResponse<String> response = PageResponse.of(records, 1, 20, 1);
        records.add("b");

        assertEquals(List.of("a"), response.getRecords());
        assertEquals(1, response.getPageNo());
        assertEquals(20, response.getPageSize());
        assertEquals(1, response.getTotal());
    }

    @Test
    void shouldRejectNullRecords() {
        assertThrows(NullPointerException.class, () -> PageResponse.of(null, 1, 20, 0));
    }
}
