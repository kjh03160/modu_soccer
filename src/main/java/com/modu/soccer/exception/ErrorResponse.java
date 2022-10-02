package com.modu.soccer.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorResponse {
	private final int code;
	private final String message;

	public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(errorCode.getMsg())
				.build()
			);
	}

	public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String msg) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(msg)
				.build()
			);
	}
}