package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Team;
import com.modu.soccer.enums.FormationName;

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
@JsonNaming(SnakeCaseStrategy.class)
public class QuarterSummary {
	private Long id;
	@JsonProperty("match_id")
	private Long matchId;
	private FormationName teamAFormation;
	private FormationName teamBFormation;
	@JsonProperty("team_a")
	private TeamScore teamA;
	@JsonProperty("team_b")
	private TeamScore teamB;
	private Integer quarter;

	public static QuarterSummary fromMatchAndQuarter(Match match, Quarter quarter) {
		return QuarterSummary.builder()
			.id(quarter.getId())
			.quarter(quarter.getQuarter())
			.matchId(quarter.getMatch().getId())
			.teamA(TeamScore.of(match.getTeamA(), quarter.getTeamAScore()))
			.teamB(TeamScore.of(match.getTeamB(), quarter.getTeamBScore()))
			.teamAFormation(quarter.getTeamAFormation())
			.teamBFormation(quarter.getTeamBFormation())
			.build();
	}

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonNaming(SnakeCaseStrategy.class)
	public static class TeamScore {
		@JsonProperty("team_id")
		private Long teamId;
		@JsonProperty("team_name")
		private String teamName;
		@JsonProperty("team_logo")
		private String teamLogo;
		@JsonProperty("team_score")
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
}
