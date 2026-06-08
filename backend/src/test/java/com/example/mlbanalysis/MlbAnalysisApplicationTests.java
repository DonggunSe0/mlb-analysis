package com.example.mlbanalysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mlbanalysis.team.client.StatsApiMlbTeamClient;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@SpringBootTest
class MlbAnalysisApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsWithPhase1NoDbProfileAndClientWiring() {
        assertThat(applicationContext.getBean(StatsApiMlbTeamClient.class)).isNotNull();
        assertThat(applicationContext.getBean(ClientHttpRequestFactory.class))
                .isInstanceOf(SimpleClientHttpRequestFactory.class);
        assertThat(applicationContext.getBeansOfType(DataSource.class)).isEmpty();
    }
}
