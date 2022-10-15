package com.modu.soccer.domain.request;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.modu.soccer.entity.Formation.TeamFormation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuarterFormationRequest {
	@JsonUnwrapped
	private TeamFormation formation;
}
