package com.modu.soccer.domain.request;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.domain.Participation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(SnakeCaseStrategy.class)
public class QuarterParticipationRequest {
	private List<Participation> participations;
	private Long quarterId;
	private Long teamId;
}
