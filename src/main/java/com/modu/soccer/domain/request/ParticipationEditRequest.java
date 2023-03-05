package com.modu.soccer.domain.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.domain.Participation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(SnakeCaseStrategy.class)
public class ParticipationEditRequest extends Participation {
	@NotNull(message = "team id should not be null")
	private Long teamId;
}
