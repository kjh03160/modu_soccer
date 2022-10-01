package com.modu.soccer.domain;

import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AuthProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
	private String email;
	private String profileURL;
	private String name;
	private boolean isPro;
	private Integer age;
	private AuthProvider authProvider;

	public static UserDto fromEntity(User user) {
		return UserDto.builder()
			.email(user.getEmail())
			.profileURL(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.authProvider(user.getAuthProvider())
			.build();
	}
}
