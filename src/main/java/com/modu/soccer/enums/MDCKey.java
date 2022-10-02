package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MDCKey {
	REQUEST_UUID("requestId"),
	USER_ID("userId"),
	CLIENT_IP("clientIp"),
	USER_AGENT("userAgent");

	private String key;
}
