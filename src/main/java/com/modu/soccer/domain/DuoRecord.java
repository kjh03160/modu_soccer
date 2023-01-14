package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class DuoRecord {
	private UserInfo user1;
	private UserInfo user2;
	private Integer count;

	public static DuoRecord of(User user1, User user2, Integer count) {
		return DuoRecord.builder()
			.user1(UserInfo.of(user1))
			.user2(UserInfo.of(user2))
			.count(count)
			.build();
	}
}
