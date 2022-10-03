package com.modu.soccer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TeamRequest {
	private String name;
	@JsonProperty("logo_url")
	private String logoUrl;

	private Double latitude;
	private Double longitude;

}
