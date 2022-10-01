package com.modu.soccer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 400
	INVALID_PARAM(HttpStatus.BAD_REQUEST, 40000, ""),
	INVALID_KAKAO_EMAIL_STATUS(HttpStatus.BAD_REQUEST, 40001, "kakao email status is invalid"),
	// 409
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, 40900, "duplicated resource"),
	DUPLICATE_USER(HttpStatus.CONFLICT, 40901, "duplicated user"),

	// 500
	KAKAO_AUTH_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "Kakao Api Error."),
	UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,  99999, "unknown error");

	private final HttpStatus httpStatus;
	private final int code;
	private final String msg;
}
