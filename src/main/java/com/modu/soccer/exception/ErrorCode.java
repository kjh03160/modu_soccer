package com.modu.soccer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 400
	INVALID_PARAM(HttpStatus.BAD_REQUEST, 40000, "%s"),
	INVALID_KAKAO_EMAIL_STATUS(HttpStatus.BAD_REQUEST, 40001, "kakao email status is invalid"),

	//401
	USER_NOT_REGISTERED(HttpStatus.UNAUTHORIZED, 40100, "user is not registered"),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 40101, "access token expired"),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 40102, "refresh token expired"),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 40103, "authentication failed"),

	// 403
	FORBIDDEN(HttpStatus.FORBIDDEN, 40300, "action forbidden"),
	NO_PERMISSION_ON_TEAM(HttpStatus.FORBIDDEN, 40301, "not permitted action on team"),

	// 404
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 40400, "%s not found"),

	// 409
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, 40900, "duplicated resource"),
	DUPLICATE_USER(HttpStatus.CONFLICT, 40901, "duplicated user"),
	ALREADY_EXIST_MEMBER(HttpStatus.CONFLICT, 40902, "user already joined to the team"),
	ALREADY_REQUESTED_JOIN(HttpStatus.CONFLICT, 40903, "user already requested join"),


	// 500
	KAKAO_AUTH_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "Kakao Api Error."),
	UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,  99999, "unknown error");

	private final HttpStatus httpStatus;
	private final int code;
	private final String msg;
}
