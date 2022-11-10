package com.modu.soccer.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamEditRequest {
	private String name;
	private Double latitude;
	private Double longitude;
}
