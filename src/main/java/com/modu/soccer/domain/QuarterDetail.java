package com.modu.soccer.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;
import com.modu.soccer.entity.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuarterDetail {
	@JsonUnwrapped
	private QuarterSummary summary;
	private List<Participation> teamAFormation;
	private List<Participation> teamBFormation;

	public static QuarterDetail fromMatchAndQuarter(Match match, Quarter quarter) {
		QuarterSummary quarterSummary = QuarterSummary.fromMatchAndQuarter(match, quarter);
		return QuarterDetail.builder()
			.summary(quarterSummary)
			.build();
	}

	public static QuarterDetail fromMatchAndQuarter(Match match, Quarter quarter,
		List<QuarterParticipation> participations) {
		QuarterSummary quarterSummary = QuarterSummary.fromMatchAndQuarter(match, quarter);
		return QuarterDetail.builder()
			.teamAFormation(extractTeamParticipations(participations, match.getTeamA()))
			.teamBFormation(extractTeamParticipations(participations, match.getTeamB()))
			.summary(quarterSummary)
			.build();
	}

	private static List<Participation> extractTeamParticipations(List<QuarterParticipation> participations, Team team) {
		return participations.stream().filter(participation -> participation.getTeam() == team)
			.map(Participation::fromEntity).toList();
	}
}
