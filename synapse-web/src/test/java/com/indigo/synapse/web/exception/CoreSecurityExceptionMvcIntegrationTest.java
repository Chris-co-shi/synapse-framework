package com.indigo.synapse.web.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class CoreSecurityExceptionMvcIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc = standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void shouldMapAuthenticationExceptionToUnifiedUnauthorizedResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth-error"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode body = body(result);
        assertEquals(CommonErrorCode.COMMON_UNAUTHORIZED.code(), body.get("code").asText());
        assertEquals(CommonErrorCode.COMMON_UNAUTHORIZED.message(), body.get("message").asText());
        assertTrue(body.get("data").isNull());
        assertFalse(body.get("traceId").asText().isBlank());
        assertFalse(body.get("timestamp").asText().isBlank());
    }

    @Test
    void shouldMapAccessDeniedExceptionToUnifiedForbiddenResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/access-denied"))
                .andExpect(status().isForbidden())
                .andReturn();

        JsonNode body = body(result);
        assertEquals(CommonErrorCode.COMMON_FORBIDDEN.code(), body.get("code").asText());
        assertEquals(CommonErrorCode.COMMON_FORBIDDEN.message(), body.get("message").asText());
        assertTrue(body.get("data").isNull());
        assertFalse(body.get("traceId").asText().isBlank());
        assertFalse(body.get("timestamp").asText().isBlank());
    }

    @Test
    void shouldMapAuthenticationExceptionWithSpecificCoreErrorCode() throws Exception {
        MvcResult result = mockMvc.perform(get("/signature-error"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode body = body(result);
        assertEquals(TestAuthenticationErrorCode.AUTH_HEADER_INVALID.code(), body.get("code").asText());
        assertEquals("签名错误", body.get("message").asText());
        assertTrue(body.get("data").isNull());
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @RestController
    static class TestController {

        @GetMapping("/auth-error")
        void authError() {
            throw new SynapseAuthenticationException();
        }

        @GetMapping("/access-denied")
        void accessDenied() {
            throw new SynapseAccessDeniedException();
        }

        @GetMapping("/signature-error")
        void signatureError() {
            throw new SynapseAuthenticationException(TestAuthenticationErrorCode.AUTH_HEADER_INVALID, "签名错误");
        }
    }

    enum TestAuthenticationErrorCode implements ErrorCode {
        AUTH_HEADER_INVALID;

        @Override
        public String code() {
            return "AUTH_HEADER_INVALID";
        }

        @Override
        public String message() {
            return "认证头无效";
        }

        @Override
        public int httpStatus() {
            return 401;
        }
    }
}
