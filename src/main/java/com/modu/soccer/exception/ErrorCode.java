package com.modu.soccer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 400
	INVALID_PARAM(HttpStatus.BAD_REQUEST, 40000, ""),
	// 409
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, 40900, "{} is duplicated"),

	// 500
	KAKAO_AUTH_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "Kakao Api Error."),
	UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,  99999, "unknown error");

	private final HttpStatus httpStatus;
	private final int code;
	private final String msg;
}
