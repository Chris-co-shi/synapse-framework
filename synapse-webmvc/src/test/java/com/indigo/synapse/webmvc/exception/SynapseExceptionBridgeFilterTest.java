package com.indigo.synapse.webmvc.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.webmvc.json.SynapseObjectMapperFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseExceptionBridgeFilterTest {

    private final SynapseExceptionBridgeFilter filter = new SynapseExceptionBridgeFilter(
            new WebErrorResponseWriter(SynapseObjectMapperFactory.create()),
            new WebExceptionResponseFactory(
                    new CompositeErrorHttpStatusResolver(
                            List.of(new CommonErrorHttpStatusResolver())
                    )
            )
    );

    @Test
    void shouldWriteUnifiedResultWhenFilterChainThrowsSynapseException() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/users"),
                response,
                chainThrowing(new SynapseAuthenticationException(TestErrorCode.SECURITY_INVALID_TRUSTED_HEADER))
        );

        JsonNode body = SynapseObjectMapperFactory.create().readTree(response.getContentAsString());

        assertEquals(401, response.getStatus());
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(response.getContentType())));
        assertEquals("SECURITY_INVALID_TRUSTED_HEADER", body.get("code").asText());
        assertEquals("非法可信请求头", body.get("message").asText());
        assertTrue(body.get("data").isNull());
        assertFalse(body.get("traceId").asText().isBlank());
        assertFalse(body.get("timestamp").asText().isBlank());
    }

    @Test
    void shouldNotSwallowNonSynapseException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> filter.doFilter(
                        new MockHttpServletRequest("GET", "/api/users"),
                        new MockHttpServletResponse(),
                        chainThrowing(new IllegalStateException("boom"))
                )
        );

        assertEquals("boom", exception.getMessage());
    }

    @Test
    void shouldNotRewriteCommittedResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getWriter().write("already committed");
        response.flushBuffer();

        assertThrows(
                SynapseAuthenticationException.class,
                () -> filter.doFilter(
                        new MockHttpServletRequest("GET", "/api/users"),
                        response,
                        chainThrowing(new SynapseAuthenticationException(TestErrorCode.SECURITY_INVALID_TRUSTED_HEADER))
                )
        );

        assertEquals("already committed", response.getContentAsString());
    }

    @Test
    void shouldPropagateFilterExceptionWithoutBridgeFilter() {
        FilterChain throwingChain = chainThrowing(
                new SynapseAuthenticationException(TestErrorCode.SECURITY_INVALID_TRUSTED_HEADER)
        );

        assertThrows(
                SynapseAuthenticationException.class,
                () -> throwingChain.doFilter(
                        new MockHttpServletRequest("GET", "/api/users"),
                        new MockHttpServletResponse()
                )
        );
    }

    private static FilterChain chainThrowing(RuntimeException exception) {
        return (request, response) -> {
            throw exception;
        };
    }

    private enum TestErrorCode implements ErrorCode {

        SECURITY_INVALID_TRUSTED_HEADER;

        @Override
        public String code() {
            return "SECURITY_INVALID_TRUSTED_HEADER";
        }

        @Override
        public String message() {
            return "非法可信请求头";
        }
    }
}
