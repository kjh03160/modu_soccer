package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.Match;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {
	@JsonProperty("team_a")
	private TeamInfo teamA;
	@JsonProperty("team_b")
	private TeamInfo teamB;
	@JsonProperty("match_date")
	private LocalDateTime matchDate;

	public static MatchDto fromEntity(Match match) {
		return MatchDto.builder()
			.teamA(TeamInfo.fromEntity(match.getTeamA()))
			.teamB(TeamInfo.fromEntity(match.getTeamB()))
			.matchDate(match.getMatchDateTime())
			.build();
	}
}
