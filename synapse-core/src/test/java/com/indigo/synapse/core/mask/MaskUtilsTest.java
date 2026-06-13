package com.indigo.synapse.core.mask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MaskUtilsTest {

    @Test
    void shouldMaskCommonValues() {
        assertEquals("张*", MaskUtils.maskName("张三"));
        assertEquals("张*明", MaskUtils.maskName("张小明"));
        assertEquals("138****5678", MaskUtils.maskMobile("13812345678"));
        assertEquals("1101**********1234", MaskUtils.maskIdCard("110101199001011234"));
        assertEquals("u**r@example.com", MaskUtils.maskEmail("user@example.com"));
    }

    @Test
    void shouldMaskShortValuesSafely() {
        assertEquals("*", MaskUtils.maskName("李"));
        assertEquals("a*@example.com", MaskUtils.maskEmail("ab@example.com"));
        assertEquals("***", MaskUtils.maskMobile("123"));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> MaskUtils.maskEmail(" "));
    }
}
