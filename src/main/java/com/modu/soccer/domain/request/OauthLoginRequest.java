package com.modu.soccer.domain.request;

import com.modu.soccer.domain.response.KakaoUserInfoResponse;
import com.modu.soccer.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OauthLoginRequest {

	private String email;
	private String profileURL;
	private String userName;
	private AuthProvider provider;
	private Integer age;

	public static OauthLoginRequest from(KakaoUserInfoResponse response) {
		OauthLoginRequest request = new OauthLoginRequest();
		request.setEmail(response.getKakaoAccount().getEmail());
		request.setUserName(response.getKakaoAccount().getProfile().getNickname());
		request.setProfileURL(response.getKakaoAccount().getProfile().getProfileImage());
		request.setProvider(AuthProvider.KAKAO);
		return request;
	}
}
