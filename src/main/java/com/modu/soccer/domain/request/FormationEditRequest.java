package com.modu.soccer.domain.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.enums.FormationName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(SnakeCaseStrategy.class)
public class FormationEditRequest {
	@NotNull(message = "team id should not be null")
	private Long teamId;
	@NotNull(message = "formation should not be null")
	private FormationName formation;
}
