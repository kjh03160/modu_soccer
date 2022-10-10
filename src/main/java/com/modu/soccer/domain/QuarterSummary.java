package com.modu.soccer.domain;

import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuarterSummary {
	private Long id;
	private Long matchId;
	private TeamScore teamA;
	private TeamScore teamB;
	private Integer quarter;

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TeamScore {
		private Long teamId;
		private String teamName;
		private String teamLogo;
		private Integer teamScore;

		public static TeamScore of(Team team, Integer score) {
			return TeamScore.builder()
				.teamId(team.getId())
				.teamLogo(team.getLogoUrl())
				.teamName(team.getName())
				.teamScore(score)
				.build();
		}
	}

	public static QuarterSummary fromMatchAndQuarter(Match match, Quarter quarter) {
		return QuarterSummary.builder()
			.id(quarter.getId())
			.quarter(quarter.getQuarter())
			.matchId(quarter.getMatch().getId())
			.teamA(TeamScore.of(match.getTeamA(), quarter.getTeamAScore()))
			.teamB(TeamScore.of(match.getTeamB(), quarter.getTeamBScore()))
			.build();
	}
}