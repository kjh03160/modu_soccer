package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AuthProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
	private String email;
	@JsonProperty("profile_url")
	private String profileURL;
	private String name;
	@JsonProperty("is_pro")
	private boolean isPro;
	private Integer age;
	@JsonProperty("auth_provider")
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
