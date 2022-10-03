package com.modu.soccer.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthenticateResponse {
	private String email;
	@JsonProperty("profile_url")
	private String profileURL;
	private String name;
	@JsonProperty("is_pro")
	private Boolean isPro;
	private Integer age;
	@JsonProperty("auth_provider")
	private AuthProvider authProvider;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("refresh_token")
	private String refreshToken;

	private static AuthenticateResponseBuilder fromEntity(User user) {
		return AuthenticateResponse.builder()
			.email(user.getEmail())
			.profileURL(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.authProvider(user.getAuthProvider())
			.refreshToken(user.getRefreshToken());
	}

	public static AuthenticateResponse of(User user, String accessToken) {
		AuthenticateResponse userDto = AuthenticateResponse.fromEntity(user)
			.accessToken(accessToken)
			.build();
		return userDto;
	}
}
