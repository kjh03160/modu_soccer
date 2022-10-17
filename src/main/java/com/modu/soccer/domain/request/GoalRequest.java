package com.modu.soccer.domain.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.sql.Time;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(SnakeCaseStrategy.class)
public class GoalRequest {
	private Long teamId;
	private Long scoringUserId;
	private Long assistUserId;
	private Time eventTime;
	private Boolean isOwnGoal;
}
