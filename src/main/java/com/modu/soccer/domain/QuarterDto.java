package com.modu.soccer.domain;

import com.modu.soccer.entity.Formation;
import com.modu.soccer.entity.Quarter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuarterDto {
	private Long id;
	private Long matchId;
	private Integer teamAScore;
	private Integer teamBScore;
	private Formation formation;
	private Integer quarter;

	public static QuarterDto fromEntity(Quarter quarter) {
		return QuarterDto.builder()
			.id(quarter.getId())
			.formation(quarter.getFormation())
			.quarter(quarter.getQuarter())
			.matchId(quarter.getMatch().getId())
			.teamAScore(quarter.getTeamAScore())
			.teamBScore(quarter.getTeamBScore())
			.build();
	}
}
