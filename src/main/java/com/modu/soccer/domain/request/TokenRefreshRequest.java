package com.modu.soccer.domain.request;

import lombok.Getter;

@Getter
public class TokenRefreshRequest {
	private String refreshToken;
}
