package com.modu.soccer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
	private ErrorCode errorCode;
	private String param;

	public CustomException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
}
