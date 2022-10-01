package com.modu.soccer.service;


import com.modu.soccer.domain.request.OauthLoginRequest;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User registerUser(OauthLoginRequest oAuthLoginRequest) {
		User user = new User();
		user.setEmail(oAuthLoginRequest.getEmail());
		user.setName(oAuthLoginRequest.getUserName());
		user.setProfileURL(oAuthLoginRequest.getProfileURL());
		user.setAge(oAuthLoginRequest.getAge());
		user.setAuthProvider(oAuthLoginRequest.getProvider());
		user.setIsPro(false);
		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException | ConstraintViolationException e) {
			throw new CustomException(ErrorCode.DUPLICATE_USER);
		}
	}

	public User oauthLogin(OauthLoginRequest oAuthLoginRequest) {
		Optional<User> optionalUser = userRepository.findByEmail(oAuthLoginRequest.getEmail());
		return optionalUser.orElseGet(() -> registerUser(oAuthLoginRequest));
	}
}
