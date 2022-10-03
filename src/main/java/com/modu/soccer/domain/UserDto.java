package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
	private String email;
	@JsonProperty("profile_url")
	private String profileURL;
	private String name;
	@JsonProperty("is_pro")
	private Boolean isPro;
	private Integer age;

	public static UserDto fromEntity(User user) {
		return UserDto.builder()
			.email(user.getEmail())
			.profileURL(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.build();
	}
}
