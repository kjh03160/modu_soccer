package com.modu.soccer.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.exception.ErrorResponse;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticateFilter extends OncePerRequestFilter {

	private JwtProvider jwtProvider;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws IOException, ServletException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		try {
			if (jwtProvider.isTokenExpired(
				jwtProvider.getJwtTokenFromHeader(authorizationHeader))) {
				ApiResponse<?> apiResponse = setResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
				objectMapper.writeValue(response.getWriter(), apiResponse);
				return;
			}
		} catch (CustomException e) {
			ApiResponse<?> apiResponse = setResponse(response, e.getErrorCode());
			objectMapper.writeValue(response.getWriter(), apiResponse);
			return;

		} catch (Exception e) {
			ApiResponse<?> apiResponse = setResponse(response, ErrorCode.UNKNOWN_ERROR);
			log.error("jwt token validate error: {}", e.getMessage());
			objectMapper.writeValue(response.getWriter(), apiResponse);
			return;
		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request)
		throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/api/v1/oauth") || path.equals("/api/v1/user/token");
	}

	private ApiResponse<?> setResponse(HttpServletResponse response, ErrorCode errorCode) {
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		return ApiResponse.errorResponse(
			ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(errorCode.getMsg())
				.build()
		);
	}
}
