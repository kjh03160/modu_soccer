package com.modu.soccer.domain.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class TeamRequest {
	private String name;
	@Setter
	private String logoUrl;
	private Double latitude;
	private Double longitude;

}
