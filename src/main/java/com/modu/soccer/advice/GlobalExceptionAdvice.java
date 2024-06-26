package com.modu.soccer.advice;


import static com.modu.soccer.exception.ErrorCode.DUPLICATE_RESOURCE;
import static com.modu.soccer.exception.ErrorCode.INVALID_PARAM;
import static com.modu.soccer.exception.ErrorCode.UNKNOWN_ERROR;

import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

	@ExceptionHandler(value = {DataIntegrityViolationException.class})
	protected ResponseEntity<ErrorResponse> handleDataException(DataIntegrityViolationException e) {
		if (e.getCause().getClass() == ConstraintViolationException.class) {
			return handleConstraintException((ConstraintViolationException) e.getCause());
		} else if (e.getCause().getClass() == PropertyValueException.class) {
			return handlePropertyValueException((PropertyValueException) e.getCause());
		}
		return handleUnKnownException(e);
	}

	@ExceptionHandler(value = {ConstraintViolationException.class})
	protected ResponseEntity<ErrorResponse> handleConstraintException(ConstraintViolationException e) {
		log.warn("ConstraintViolationException: {}", e.getMessage());
		return ErrorResponse.toResponseEntity(DUPLICATE_RESOURCE);
	}

	@ExceptionHandler(value = {PropertyValueException.class})
	protected ResponseEntity<ErrorResponse> handlePropertyValueException(PropertyValueException e) {
		log.warn("PropertyValueException: {}", e.getMessage());
		return ErrorResponse.toResponseEntity(INVALID_PARAM, "invalid param");
	}

	@ExceptionHandler(value = {
		IllegalArgumentException.class,
		MethodArgumentTypeMismatchException.class,

	})
	protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(
		Exception e) {
		log.info(e.getMessage());
		return ErrorResponse.toResponseEntity(INVALID_PARAM, "invalid param");
	}

	@ExceptionHandler(value = {HttpMessageNotReadableException.class})
	protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
		HttpMessageNotReadableException e) {
		log.warn(e.getMessage());
		return ErrorResponse.toResponseEntity(INVALID_PARAM, "invalid request");
	}

	@ExceptionHandler(value = {CustomException.class})
	protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		if (e.getErrorCode().getHttpStatus().value() >= 500) {
			log.error("handleCustomException throw CustomException : {}", e.getErrorCode());
		}
		if (e.getParam() != null) {
			return ErrorResponse.toResponseEntity(e.getErrorCode(), e.getParam());
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
		log.error("Unknown Exception: {}", ExceptionUtils.getStackTrace(e));
		return ErrorResponse.toResponseEntity(UNKNOWN_ERROR);
	}
}