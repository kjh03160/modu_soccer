package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum AuthProvider {
	KAKAO("KAKAO");

	private String authProvider;
}
