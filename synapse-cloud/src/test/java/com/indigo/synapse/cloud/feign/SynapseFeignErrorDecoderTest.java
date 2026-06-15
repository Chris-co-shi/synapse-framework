package com.indigo.synapse.cloud.feign;

import com.indigo.synapse.cloud.remote.RemoteCallException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseFeignErrorDecoderTest {

    private final SynapseFeignErrorDecoder decoder = new SynapseFeignErrorDecoder();

    @Test
    void shouldDecodeStandardResultErrorBody() {
        Response response = response(400, """
                {"code":"ORDER_BAD_REQUEST","message":"invalid order","traceId":"trace-remote"}
                """);

        Exception exception = decoder.decode("OrderClient#get", response);

        assertThat(exception).isInstanceOf(RemoteCallException.class);
        RemoteCallException remote = (RemoteCallException) exception;
        assertThat(remote.status()).isEqualTo(400);
        assertThat(remote.methodKey()).isEqualTo("OrderClient#get");
        assertThat(remote.remoteCode()).isEqualTo("ORDER_BAD_REQUEST");
        assertThat(remote.remoteMessage()).isEqualTo("invalid order");
        assertThat(remote.remoteTraceId()).isEqualTo("trace-remote");
        assertThat(remote.bodySummary()).contains("ORDER_BAD_REQUEST");
    }

    @Test
    void shouldFallbackWhenBodyIsNotJson() {
        Exception exception = decoder.decode("OrderClient#get", response(502, "bad gateway"));

        RemoteCallException remote = (RemoteCallException) exception;
        assertThat(remote.status()).isEqualTo(502);
        assertThat(remote.remoteCode()).isNull();
        assertThat(remote.remoteTraceId()).isNull();
        assertThat(remote.bodySummary()).isEqualTo("bad gateway");
    }

    @Test
    void shouldFallbackWhenBodyIsEmpty() {
        Exception exception = decoder.decode("OrderClient#get", response(500, null));

        RemoteCallException remote = (RemoteCallException) exception;
        assertThat(remote.status()).isEqualTo(500);
        assertThat(remote.remoteCode()).isNull();
        assertThat(remote.remoteMessage()).isNull();
        assertThat(remote.bodySummary()).isNull();
    }

    private Response response(int status, String body) {
        Response.Builder builder = Response.builder()
                .status(status)
                .reason("remote error")
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "http://remote/orders/1",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ));
        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return builder.build();
    }
}
