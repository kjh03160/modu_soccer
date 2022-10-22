package com.modu.soccer.service;

import com.modu.soccer.domain.request.OauthLoginRequest;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.TokenType;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.jwt.JwtProvider;
import com.modu.soccer.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;

	public User registerUser(OauthLoginRequest oAuthLoginRequest) {
		User user = User.builder()
			.name(oAuthLoginRequest.getUserName())
			.email(oAuthLoginRequest.getEmail())
			.profileURL(oAuthLoginRequest.getProfileURL())
			.authProvider(oAuthLoginRequest.getProvider())
			.isPro(false)
			.age(oAuthLoginRequest.getAge())
			.build();
		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException | ConstraintViolationException e) {
			throw new CustomException(ErrorCode.DUPLICATE_USER);
		}
	}

	@Transactional
	public User oauthLogin(OauthLoginRequest oAuthLoginRequest) {
		Optional<User> optionalUser = userRepository.findByEmail(oAuthLoginRequest.getEmail());
		User user = optionalUser.orElseGet(() -> registerUser(oAuthLoginRequest));
		user.setRefreshToken(jwtProvider.createTokenOfType(user, TokenType.AUTH_REFRESH_TOKEN));
		return user;
	}

	@Transactional
	public User refreshUserToken(String accessToken, String refreshToken) {
		Long accessTokenUserId = jwtProvider.getUserId(accessToken);
		Long refreshTokenUserId = jwtProvider.getUserId(refreshToken);
		if (!accessTokenUserId.equals(refreshTokenUserId)) {
			throw new IllegalArgumentException("different user token");
		}
		User user = userRepository.findById(refreshTokenUserId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.USER_NOT_REGISTERED);
		});

		if (!user.getRefreshToken().equals(refreshToken) || jwtProvider.isTokenExpired(refreshToken)) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		}
		user.setRefreshToken(jwtProvider.createTokenOfType(user, TokenType.AUTH_REFRESH_TOKEN));
		return user;
	}
}
