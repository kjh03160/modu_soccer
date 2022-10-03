package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
	private Long id;
	private UserDto owner;
	private String name;
	@JsonProperty("logo_url")
	private String logoUrl;
	private TeamRecordDto record;
	private PointResponse location;

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class PointResponse {
		private Double longitude;
		private Double latitude;
	}

	public static TeamDto fromEntity(Team team){
		PointResponse point = null;
		if (team.getLocation() != null) {
			point = new PointResponse(team.getLocation().getX(),
				team.getLocation().getY());
		}
		return TeamDto.builder()
			.id(team.getId())
			.location(point)
			.name(team.getName())
			.logoUrl(team.getLogoUrl())
			.owner(UserDto.fromEntity(team.getOwner()))
			.record(TeamRecordDto.fromEntity(team.getRecord()))
			.build();
	}
}
