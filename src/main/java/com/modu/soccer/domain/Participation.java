package com.modu.soccer.domain;

import java.sql.Time;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.enums.Position;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class Participation {
	@NotNull(message = "in user id should not be null")
	private Long inUserId;
	@NotEmpty(message = "in user name should not be empty")
	private String inUserName;
	private Long outUserId;
	private String outUserName;
	@NotNull(message = "position should not be null")
	private Position position;
	@NotNull(message = "event time should not be null")
	private Time eventTime;

	public static Participation fromEntity(QuarterParticipation participation) {
		return Participation.builder()
			.inUserId(participation.getInUser().getId())
			.inUserName(participation.getInUserName())
			.outUserId(participation.getOutUser() != null ? participation.getOutUser().getId() : null)
			.outUserName(participation.getOutUserName())
			.eventTime(participation.getEventTime())
			.position(participation.getPosition())
			.build();
	}

	public QuarterParticipation toEntity(Quarter quarter, Team team, TeamMember inTeamMember,
		TeamMember outTeamMember) {
		return QuarterParticipation.builder()
			.quarter(quarter)
			.team(team)
			.eventTime(this.getEventTime())
			.inUser(inTeamMember.getUser())
			.inUserName(this.getInUserName())
			.outUser(outTeamMember != null ? outTeamMember.getUser() : null)
			.outUserName(this.outUserName)
			.position(this.position)
			.build();
	}
}
