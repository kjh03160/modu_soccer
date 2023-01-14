package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.User;
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
public class SoloRecordDto {
	private UserInfo user;
	private Integer count;

	public static SoloRecordDto from(User user, Integer count) {
		return SoloRecordDto.builder()
			.user(UserInfo.of(user))
			.count(count)
			.build();
	}
}
