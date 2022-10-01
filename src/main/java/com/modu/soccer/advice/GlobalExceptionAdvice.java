package com.modu.soccer.advice;


import static com.modu.soccer.exception.ErrorCode.DUPLICATE_RESOURCE;
import static com.modu.soccer.exception.ErrorCode.INVALID_PARAM;
import static com.modu.soccer.exception.ErrorCode.UNKNOWN_ERROR;

import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

	@ExceptionHandler(value = {ConstraintViolationException.class,
		DataIntegrityViolationException.class})
	protected ResponseEntity<ErrorResponse> handleDataException() {
		return ErrorResponse.toResponseEntity(DUPLICATE_RESOURCE);
	}

	@ExceptionHandler(value = {CustomException.class})
	protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		if (e.getErrorCode().getHttpStatus().value() >= 500) {
			log.error("handleCustomException throw CustomException : {}", e.getErrorCode());
		}
		return ErrorResponse.toResponseEntity(e.getErrorCode());
	}


	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	protected ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(
		MethodArgumentNotValidException e) {
		String validationMsg = e.getFieldErrors().stream()
			.map(fe -> fe.getField() + " " + fe.getRejectedValue() + " " + fe.getDefaultMessage())
			.collect(Collectors.joining(", "));
		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(INVALID_PARAM.getCode())
			.message(validationMsg)
			.build();
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = {Exception.class})
	protected ResponseEntity<ErrorResponse> handleUnKnownException(Exception e) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Unknown Exception : %s\n", e.getMessage()));
		StackTraceElement[] stackTrace = e.getStackTrace();
		builder.append(stackTrace[0].toString());
		log.error(builder.toString());
		return ErrorResponse.toResponseEntity(UNKNOWN_ERROR);
	}
}