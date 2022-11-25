package com.modu.soccer.service;

import com.modu.soccer.domain.request.UserInfoRequest;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public User getUser(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user");
		});
	}

	@Transactional
	public void editUserInfo(User user, UserInfoRequest request) {
		user.setName(request.getName());
		user.setIsPro(request.getIsPro());
		user.setAge(request.getAge());
	}

	@Transactional
	public void editUserProfile(User user, String profileUrl) {
		user.setProfileURL(profileUrl);
	}
}
