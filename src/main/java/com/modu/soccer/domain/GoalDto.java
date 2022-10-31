package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.Goal;
import java.sql.Time;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class GoalDto {
	private Long goalId;
	private UserInfo assistant;
	private UserInfo scorer;
	private Boolean isOwnGoal;
	private Time eventTime;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	static class UserInfo {
		private Long userId;
		private String name;

		static UserInfo of(Long id, String name) {
			return UserInfo.builder()
				.userId(id)
				.name(name)
				.build();
		}
	}

	public static GoalDto fromEntity(Goal goal) {
		return GoalDto.builder()
			.goalId(goal.getId())
			.assistant(UserInfo.of(goal.getAssistUser().getId(), goal.getAssistantName()))
			.scorer(UserInfo.of(goal.getScoringUser().getId(), goal.getScorerName()))
			.isOwnGoal(goal.getIsOwnGoal())
			.eventTime(goal.getEventTime())
			.build();
	}
}
