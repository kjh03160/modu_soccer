package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class UserDto {
	private String email;
	private String profileUrl;
	private String name;
	private Boolean isPro;
	private Integer age;

	public static UserDto fromEntity(User user) {
		return UserDto.builder()
			.email(user.getEmail())
			.profileUrl(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.build();
	}
}
