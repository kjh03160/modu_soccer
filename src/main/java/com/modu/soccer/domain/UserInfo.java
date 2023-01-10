package com.modu.soccer.domain;

import com.modu.soccer.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserInfo {

	private Long userId;
	private String name;

	static UserInfo of(User user) {
		return UserInfo.builder()
			.userId(user.getId())
			.name(user.getName())
			.build();
	}
}
