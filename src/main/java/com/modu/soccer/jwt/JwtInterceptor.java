package com.modu.soccer.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.MDCKey;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.UserContextUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;
	private final ObjectMapper mapper;
	private static final String CONTENT_TYPE = "application/json";
	private static final String ENCODING = "utf-8";


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception {
		try {
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			String token = jwtProvider.getJwtTokenFromHeader(authorizationHeader);
			if (jwtProvider.isTokenExpired(token)) {
				throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
			}
			Long userId = jwtProvider.getUserId(token);
			MDC.put(MDCKey.USER_ID.getKey(), userId.toString());
			User user = userRepository.findById(userId).orElseThrow(() -> {
				throw new CustomException(ErrorCode.USER_NOT_REGISTERED);
			});
			UserContextUtil.setUser(user);
		} catch (CustomException e) {
			log.warn(e.getMessage());
			handleCustomException(request, response, e);
			return false;
		} catch (Exception e) {
			log.error("jwt interceptor raise unknown exception: {}", e.getMessage());
			handleException(request, response, e);
			return false;
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
		throws Exception {
	}

	private void handleCustomException(HttpServletRequest request, HttpServletResponse response, CustomException ex)
		throws IOException {
		ErrorCode errorCode = ex.getErrorCode();
		HttpStatus httpStatus = errorCode.getHttpStatus();

		writeResponse(response, httpStatus, errorCode.getCode(), errorCode.getMsg());

	}

	private void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex)
		throws IOException {
		ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;
		HttpStatus httpStatus = errorCode.getHttpStatus();

		writeResponse(response, httpStatus, errorCode.getCode(), errorCode.getMsg());
	}

	private void writeResponse(HttpServletResponse response, HttpStatus status, Integer code, String message)
		throws IOException {
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("code", code);
		responseBody.put("message", message);
		String s = mapper.writeValueAsString(responseBody);
		response.setContentType(CONTENT_TYPE);
		response.setCharacterEncoding(ENCODING);
		response.setStatus(status.value());
		response.getWriter().write(s);
	}
}
