package com.example.mlbanalysis.teamplayer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mlbanalysis.common.error.MlbApiException;
import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterEntryDto;
import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterPersonDto;
import com.example.mlbanalysis.teamplayer.client.dto.MlbRosterPositionDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class TeamPlayerServiceTest {

    @Test
    void mapsProviderRosterIntoPublicEnvelope() {
        var service = new TeamPlayerService(teamId -> List.of(new MlbRosterEntryDto(
                new MlbRosterPersonDto(675961, "Alika Williams"),
                "12",
                new MlbRosterPositionDto("4", "Second Base", "Infielder", "2B")
        )));

        var response = service.getTeamPlayers(133);

        assertThat(response.teamId()).isEqualTo(133);
        assertThat(response.players()).hasSize(1);
        assertThat(response.players().getFirst().playerId()).isEqualTo(675961);
        assertThat(response.players().getFirst().fullName()).isEqualTo("Alika Williams");
        assertThat(response.players().getFirst().jerseyNumber()).isEqualTo("12");
        assertThat(response.players().getFirst().position()).isEqualTo("Second Base");
    }

    @Test
    void mapsEmptyRosterIntoEmptyEnvelope() {
        var response = new TeamPlayerService(teamId -> List.of()).getTeamPlayers(133);

        assertThat(response.teamId()).isEqualTo(133);
        assertThat(response.players()).isEmpty();
    }

    @Test
    void handlesMissingNestedFieldsWithoutNpe() {
        var response = new TeamPlayerService(teamId -> List.of(new MlbRosterEntryDto(
                null, null, null
        ))).getTeamPlayers(133);

        assertThat(response.players().getFirst().playerId()).isNull();
        assertThat(response.players().getFirst().position()).isNull();
    }

    @Test
    void propagatesClientFailureForControllerAdvice() {
        TeamPlayerService service = new TeamPlayerService(teamId -> {
            throw new MlbApiException("provider unavailable");
        });

        assertThatThrownBy(() -> service.getTeamPlayers(133)).isInstanceOf(MlbApiException.class);
    }
}
