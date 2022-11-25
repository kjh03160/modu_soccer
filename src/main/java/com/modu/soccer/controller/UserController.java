package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.UserInfo;
import com.modu.soccer.domain.request.UserInfoRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import com.modu.soccer.service.S3UploadService;
import com.modu.soccer.service.TeamService;
import com.modu.soccer.service.UserService;
import com.modu.soccer.utils.UserContextUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;
	private final TeamService teamService;
	public final S3UploadService s3UploadService;

	@GetMapping("/me")
	public ApiResponse<?> getCurrentUserInfo() {
		User user = UserContextUtil.getCurrentUser();
		List<Team> teams = teamService.getTeamsOfUser(user);
		return ApiResponse.withBody(UserInfo.of(user, teams));
	}

	@PutMapping("/me")
	public ApiResponse<?> putCurrentUserInfo(@RequestBody UserInfoRequest request) {
		User user = UserContextUtil.getCurrentUser();
		userService.editUserInfo(user, request);
		return ApiResponse.ok();
	}

	@PutMapping("/me/profile")
	public ApiResponse<?> putCurrentUserProfile(@RequestPart("file") MultipartFile file) {
		User user = UserContextUtil.getCurrentUser();
		String prevProfile = user.getProfileURL();
		String filePath = s3UploadService.uploadFile(file);
		userService.editUserProfile(user, filePath);
		s3UploadService.deleteFile(prevProfile);
		return ApiResponse.ok();
	}

	@GetMapping("/{user_id}")
	public ApiResponse<?> getUserInfo(@PathVariable("user_id") long userId) {
		User user = userService.getUser(userId);
		List<Team> teams = teamService.getTeamsOfUser(user);
		return ApiResponse.withBody(UserInfo.of(user, teams));
	}
}
