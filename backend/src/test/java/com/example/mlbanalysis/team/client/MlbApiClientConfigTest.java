package com.example.mlbanalysis.team.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mlbanalysis.common.config.MlbApiClientConfig;
import com.example.mlbanalysis.common.config.MlbApiProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class MlbApiClientConfigTest {

    @Test
    void clientRequestFactoryUsesBoundedTimeoutProperties() throws Exception {
        MlbApiProperties properties = new MlbApiProperties();
        properties.setConnectTimeout(Duration.ofMillis(1234));
        properties.setReadTimeout(Duration.ofMillis(5678));

        Object factory = new MlbApiClientConfig().mlbClientHttpRequestFactory(properties);

        assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
        assertThat(intField(factory, "connectTimeout")).isEqualTo(1234);
        assertThat(intField(factory, "readTimeout")).isEqualTo(5678);
    }

    @Test
    void clientAddsProviderFriendlyDefaultHeaders() {
        MlbApiProperties properties = new MlbApiProperties();
        properties.setBaseUrl("https://statsapi.mlb.test/api/v1");
        properties.setClientName("mlb-analysis-test");
        properties.setContactEmail("ops@example.com");
        var requestFactory = new RecordingRequestFactory();
        var config = new MlbApiClientConfig();
        RestClient restClient = config.mlbRestClient(properties, requestFactory, RestClient.builder());

        restClient.get().uri("/ping").retrieve().toBodilessEntity();

        assertThat(requestFactory.uri).isEqualTo(URI.create("https://statsapi.mlb.test/api/v1/ping"));
        assertThat(requestFactory.headers.getFirst(HttpHeaders.ACCEPT)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(requestFactory.headers.getFirst(HttpHeaders.USER_AGENT)).isEqualTo("mlb-analysis-test (ops@example.com)");
    }

    private int intField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(target);
    }

    private static class RecordingRequestFactory implements ClientHttpRequestFactory {
        private URI uri;
        private HttpHeaders headers;

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
            this.uri = uri;
            return new ClientHttpRequest() {
                private final HttpHeaders requestHeaders = new HttpHeaders();
                private final Map<String, Object> attributes = new HashMap<>();

                @Override
                public Map<String, Object> getAttributes() {
                    return attributes;
                }

                @Override
                public HttpMethod getMethod() {
                    return httpMethod;
                }

                @Override
                public URI getURI() {
                    return uri;
                }

                @Override
                public HttpHeaders getHeaders() {
                    return requestHeaders;
                }

                @Override
                public OutputStream getBody() {
                    return new ByteArrayOutputStream();
                }

                @Override
                public ClientHttpResponse execute() {
                    headers = requestHeaders;
                    return new ClientHttpResponse() {
                        @Override
                        public HttpStatusCode getStatusCode() {
                            return HttpStatus.OK;
                        }

                        @Override
                        public String getStatusText() {
                            return "OK";
                        }

                        @Override
                        public void close() {
                        }

                        @Override
                        public InputStream getBody() {
                            return new ByteArrayInputStream("{}".getBytes());
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders responseHeaders = new HttpHeaders();
                            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
                            return responseHeaders;
                        }
                    };
                }
            };
        }
    }
}
