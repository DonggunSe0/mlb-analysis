package com.example.mlbanalysis.team.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mlbanalysis.common.config.MlbApiClientConfig;
import com.example.mlbanalysis.common.config.MlbApiProperties;
import java.lang.reflect.Field;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

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

    private int intField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(target);
    }
}
