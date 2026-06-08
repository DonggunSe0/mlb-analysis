package com.example.mlbanalysis.team.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mlbanalysis.common.cache.CacheConfig;
import com.example.mlbanalysis.team.client.MlbTeamClient;
import com.example.mlbanalysis.team.client.dto.MlbTeamDto;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {CacheConfig.class, TeamService.class, TeamServiceCachingTest.TestClientConfig.class})
class TeamServiceCachingTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private CountingTeamClient teamClient;

    @Test
    void cachesTeamListResponses() {
        var first = teamService.getTeams();
        var second = teamService.getTeams();

        assertThat(first.teams()).hasSize(1);
        assertThat(second.teams()).hasSize(1);
        assertThat(teamClient.calls()).isEqualTo(1);
    }

    @Configuration
    static class TestClientConfig {
        @Bean
        CountingTeamClient countingTeamClient() {
            return new CountingTeamClient();
        }
    }

    static class CountingTeamClient implements MlbTeamClient {
        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public List<MlbTeamDto> getTeams() {
            calls.incrementAndGet();
            return List.of(new MlbTeamDto(133, "Athletics", "ATH", "Athletics", "Sacramento", null, null, null, true));
        }

        int calls() {
            return calls.get();
        }
    }
}
