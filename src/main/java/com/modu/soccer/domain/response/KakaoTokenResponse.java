package com.modu.soccer.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class KakaoTokenResponse {
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private Integer expiresIn;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("refresh_token_expires_in")
	private Integer refreshTokenExpiresIn;
}
