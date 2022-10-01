package com.modu.soccer.service;

import com.modu.soccer.domain.response.KakaoTokenResponse;
import com.modu.soccer.domain.response.KakaoUserInfoResponse;
import com.modu.soccer.domain.response.KakaoUserInfoResponse.KakaoAccount;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoOauthService {

	private final String TOKEN_REQUEST_URL = "/oauth/token";
	private final String USER_INFO_REQUEST_URL = "/v2/user/me";
	@Value("${oauth.kakao.auth_host}")
	private String AUTH_HOST;
	@Value("${oauth.kakao.api_host}")
	private String API_HOST;
	@Value("${oauth.kakao.client_id}")
	private String CLIENT_ID;
	@Value("${oauth.kakao.client_secret}")
	private String CLIENT_SECRET;
	@Value("${oauth.kakao.redirect_uri}")
	private String REDIRECT_URI;
	private final RestTemplate restTemplate;

	public String requestOauthToken(String code) {
		String requestURI = AUTH_HOST + TOKEN_REQUEST_URL;
		HttpHeaders headers = getKakaoDefaultHeader();

		MultiValueMap<String, String> params = getTokenRequestParams(code);

		ResponseEntity<KakaoTokenResponse> responseEntity = restTemplate.exchange(requestURI,
			HttpMethod.POST, new HttpEntity<>(params, headers),
			KakaoTokenResponse.class);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			KakaoTokenResponse body = responseEntity.getBody();
			assert body != null;
			return body.getAccessToken();
		} else {
			log.error("Get kakao oauth token failed, response {}", responseEntity);
			throw new CustomException(ErrorCode.KAKAO_AUTH_INTERNAL_ERROR);
		}
	}

	public KakaoUserInfoResponse getUserInfo(String accessToken) throws CustomException {
		String requestURI = API_HOST + USER_INFO_REQUEST_URL;
		HttpHeaders headers = getKakaoDefaultHeader();
		headers.setBearerAuth(accessToken);

		ResponseEntity<KakaoUserInfoResponse> responseEntity = restTemplate.exchange(requestURI,
			HttpMethod.GET, new HttpEntity<>(headers),
			KakaoUserInfoResponse.class);
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			validateResponse(responseEntity);
			log.info(responseEntity.getBody().toString());
			return responseEntity.getBody();
		}

		log.error("Get Kakao User Info Failed, status code: {}, body: {}",
			responseEntity.getStatusCode(), responseEntity.getBody());
		throw new CustomException(ErrorCode.KAKAO_AUTH_INTERNAL_ERROR);
	}

	private static HttpHeaders getKakaoDefaultHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add("charset", "utf-8");
		return headers;
	}

	private MultiValueMap<String, String> getTokenRequestParams(String code) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", CLIENT_ID);
		params.add("redirect_uri", REDIRECT_URI);
		params.add("code", code);
		params.add("client_secret", CLIENT_SECRET);
		return params;
	}

	private void validateResponse(ResponseEntity<KakaoUserInfoResponse> responseEntity) {
		KakaoUserInfoResponse kakaoUserInfoResponse = responseEntity.getBody();
		Objects.requireNonNull(kakaoUserInfoResponse);
		if (kakaoUserInfoResponse.getKakaoAccount() == null
			|| kakaoUserInfoResponse.getKakaoAccount().getProfile() == null) {
			log.error("kakao user info malformed value, response: {}", responseEntity);
			throw new CustomException(ErrorCode.KAKAO_AUTH_INTERNAL_ERROR);
		}
		if (!isValidEmail(kakaoUserInfoResponse.getKakaoAccount())) {
			throw new CustomException(ErrorCode.INVALID_KAKAO_EMAIL_STATUS);
		}
	}
	private boolean isValidEmail(KakaoAccount kakaoAccount) {
		return kakaoAccount.getIsEmailValid() && kakaoAccount.getIsEmailVerified();
	}
}
