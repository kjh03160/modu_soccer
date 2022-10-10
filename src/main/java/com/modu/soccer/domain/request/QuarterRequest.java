package com.modu.soccer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuarterRequest {
	private Integer quarter;
	@JsonProperty("team_a_score")
	private Integer teamAScore;
	@JsonProperty("team_b_score")
	private Integer teamBScore;
}
