package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class TeamInfo {
	private Long teamId;
	private String logo;
	private String name;
	private TeamRecordDto record;

	static TeamInfo fromEntity(Team team) {
		return TeamInfo.builder()
			.teamId(team.getId())
			.logo(team.getLogoUrl())
			.record(TeamRecordDto.fromEntity(team.getRecord()))
			.name(team.getName())
			.build();
	}
}