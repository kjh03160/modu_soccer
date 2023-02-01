package com.modu.soccer.domain;

import java.sql.Time;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.QuarterParticipation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class Participation {
	private Long inUserId;
	private String inUserName;
	private Long outUserId;
	private String outUserName;
	private Time eventTime;

	public static Participation fromEntity(QuarterParticipation participation) {
		return Participation.builder()
			.inUserId(participation.getInUser().getId())
			.inUserName(participation.getInUserName())
			.outUserId(participation.getOutUser().getId())
			.outUserName(participation.getOutUserName())
			.eventTime(participation.getEventTime())
			.build();
	}
}
