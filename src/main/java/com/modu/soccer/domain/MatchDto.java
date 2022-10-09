package com.modu.soccer.domain;

import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Team;
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
	private TeamInfo teamA;
	private TeamInfo teamB;
	private LocalDateTime matchDate;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	static class TeamInfo {
		private Long teamId;
		private String logo;
		private String name;

		static TeamInfo fromEntity(Team team) {
			return TeamInfo.builder()
				.teamId(team.getId())
				.logo(team.getLogoUrl())
				.name(team.getName())
				.build();
		}
	}

	public static MatchDto fromEntity(Match match) {
		return MatchDto.builder()
			.teamA(TeamInfo.fromEntity(match.getTeamA()))
			.teamB(TeamInfo.fromEntity(match.getTeamB()))
			.matchDate(match.getMatchDateTime())
			.build();
	}
}
