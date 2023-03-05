package com.modu.soccer.domain.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.domain.Participation;
import lombok.Getter;

@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class ParticipationEditRequest extends Participation {
	private Long teamId;
}
