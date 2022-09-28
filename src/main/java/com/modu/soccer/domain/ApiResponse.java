package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modu.soccer.exception.ErrorResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private static final int SUCCESS_STATUS = 0;
	private static final String SUCCESS_MESSAGE = "success";

	private int code;
	private String message;
	private T contents;

	public static ApiResponse<?> errorResponse(ErrorResponse err) {
		ApiResponse<Object> response = new ApiResponse<>();
		response.code = err.getCode();
		response.message = err.getMessage();
		return response;
	}

	public static ApiResponse<?> ok() {
		ApiResponse<Object> response = new ApiResponse<>();
		response.code = SUCCESS_STATUS;
		response.message = SUCCESS_MESSAGE;
		return response;
	}

	public static <T> ApiResponse<?> withBody(T body) {
		ApiResponse<T> response = new ApiResponse<T>();
		response.code = SUCCESS_STATUS;
		response.message = SUCCESS_MESSAGE;
		response.contents = body;
		return response;
	}
}
