package com.modu.soccer.domain.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamJoinRequest {
	@JsonProperty("team_id")
	private Long teamId;
}
