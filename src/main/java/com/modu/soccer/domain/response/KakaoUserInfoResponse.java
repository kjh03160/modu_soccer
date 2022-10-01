package com.modu.soccer.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import java.util.function.BinaryOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
public class KakaoUserInfoResponse {
	private Long id;
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;


	@Data
	@NoArgsConstructor
	public static class KakaoAccount {
		@JsonProperty("is_email_valid")
		private Boolean isEmailValid;
		@JsonProperty("is_email_verified")
		private Boolean isEmailVerified;
		private String email;
		private KakaoProfile profile;

		@Data
		@NoArgsConstructor
		public static class KakaoProfile {
			@JsonProperty("profile_image_url")
			private String profileImage;
			private String nickname;
		}
	}
}

