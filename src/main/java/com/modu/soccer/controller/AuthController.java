package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.request.OauthLoginRequest;
import com.modu.soccer.domain.request.TokenRefreshRequest;
import com.modu.soccer.domain.response.AuthenticateResponse;
import com.modu.soccer.domain.response.KakaoUserInfoResponse;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.TokenType;
import com.modu.soccer.jwt.JwtProvider;
import com.modu.soccer.service.AuthService;
import com.modu.soccer.service.KakaoOauthService;
import com.modu.soccer.utils.MDCUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

	private final KakaoOauthService kakaoOauthService;
	private final AuthService authService;
	private final JwtProvider jwtProvider;

	@GetMapping("/oauth/callback/kakao")
	public ApiResponse<?> kakaoCallback(@RequestParam String code) {
		String token = kakaoOauthService.requestOauthToken(code);
		KakaoUserInfoResponse userInfo = kakaoOauthService.getUserInfo(token);
		User user = authService.oauthLogin(OauthLoginRequest.from(userInfo));
		return ApiResponse.withBody(
			AuthenticateResponse.of(user, jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)));
	}

	@PostMapping("/user/token")
	public ApiResponse<?> refreshAccessToken(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION) String header,
		@RequestBody TokenRefreshRequest request
	) {
		if (!jwtProvider.isTokenExpired(jwtProvider.getJwtTokenFromHeader(header))) {
			throw new IllegalArgumentException("token is not expired.");
		}
		Long userId = MDCUtil.getUserIdFromMDC();
		User user = authService.refreshUserToken(userId, request.getRefreshToken());
		return ApiResponse.withBody(
			AuthenticateResponse.of(user, jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)));
	}
}



