package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.response.KakaoUserInfoResponse;
import com.modu.soccer.domain.request.OauthLoginRequest;
import com.modu.soccer.domain.UserDto;
import com.modu.soccer.entity.User;
import com.modu.soccer.service.KakaoOauthService;
import com.modu.soccer.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OauthController {

	private final KakaoOauthService kakaoOauthService;
	private final UserService userService;

	@GetMapping("/oauth/callback/kakao")
	public ApiResponse<?> kakaoCallback(@RequestParam String code) {
		String token = kakaoOauthService.requestOauthToken(code);
		KakaoUserInfoResponse userInfo = kakaoOauthService.getUserInfo(token);
		User user = userService.oauthLogin(OauthLoginRequest.from(userInfo));
		return ApiResponse.withBody(UserDto.fromEntity(user));
	}
}



