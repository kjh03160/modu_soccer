package com.modu.soccer.jwt;

import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.UserContextUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		String token = jwtProvider.getJwtTokenFromHeader(authorizationHeader);
		if (jwtProvider.isTokenExpired(token)) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
		}
		User user = userRepository.findById(jwtProvider.getUserId(token)).orElseThrow(() -> {
			throw new CustomException(ErrorCode.USER_NOT_REGISTERED);
		});
		UserContextUtil.setUser(user);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
		throws Exception {
		UserContextUtil.clear();
	}
}
