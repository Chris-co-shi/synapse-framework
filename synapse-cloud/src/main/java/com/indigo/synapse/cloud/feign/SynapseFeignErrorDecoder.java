package com.indigo.synapse.cloud.feign;

import com.indigo.synapse.cloud.remote.RemoteCallException;
import com.indigo.synapse.cloud.remote.RemoteErrorBodyParser;
import com.indigo.synapse.cloud.remote.RemoteErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Feign 远程错误响应解码器。
 *
 * <p>该解码器不依赖 WebMVC / WebFlux Result，只从响应体中提取 code、message、traceId 等通用字段，
 * 非标准 JSON 或空 body 会降级为通用远程调用异常。</p>
 */
public final class SynapseFeignErrorDecoder implements ErrorDecoder {

    private final RemoteErrorBodyParser bodyParser;

    public SynapseFeignErrorDecoder() {
        this(new RemoteErrorBodyParser());
    }

    public SynapseFeignErrorDecoder(RemoteErrorBodyParser bodyParser) {
        this.bodyParser = bodyParser == null ? new RemoteErrorBodyParser() : bodyParser;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response == null ? -1 : response.status();
        RemoteErrorResponse remoteErrorResponse = bodyParser.parse(body(response));
        return new RemoteCallException(methodKey, status, remoteErrorResponse);
    }

    private String body(Response response) {
        if (response == null || response.body() == null) {
            return null;
        }
        try (InputStream inputStream = response.body().asInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return null;
        }
    }
}
