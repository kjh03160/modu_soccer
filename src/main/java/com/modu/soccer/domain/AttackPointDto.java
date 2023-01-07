package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.AttackPoint;
import com.modu.soccer.entity.User;
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
public class AttackPointDto {

	private Long goalId;
	private Long teamId;
	private UserInfo assistant;
	private UserInfo scorer;
	private Boolean isOwnGoal;
	private Time eventTime;

	public static AttackPointDto fromEntity(AttackPoint attackPoint) {
		return AttackPointDto.builder()
			.teamId(attackPoint.getTeam().getId())
			.goalId(attackPoint.getId())
			.eventTime(attackPoint.getEventTime())
			.assistant(attackPoint.getAssist() == null ? null
				: UserInfo.of(attackPoint.getAssist().getUser()))
			.scorer(UserInfo.of(attackPoint.getUser()))
			.isOwnGoal(attackPoint.isOwnGoal())
			.build();
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	static class UserInfo {

		private Long userId;
		private String name;

		static UserInfo of(User user) {
			return UserInfo.builder()
				.userId(user.getId())
				.name(user.getName())
				.build();
		}
	}
}
