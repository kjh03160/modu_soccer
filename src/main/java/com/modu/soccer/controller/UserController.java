package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.UserInfo;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import com.modu.soccer.service.TeamService;
import com.modu.soccer.service.UserService;
import com.modu.soccer.utils.UserContextUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
	private final UserService userService;
	private final TeamService teamService;

	@GetMapping("/me")
	public ApiResponse<?> getCurrentUserInfo() {
		User user = UserContextUtil.getCurrentUser();
		List<Team> teams = teamService.getTeamsOfUser(user);
		return ApiResponse.withBody(UserInfo.of(user, teams));
	}

	@GetMapping("/{user_id}")
	public ApiResponse<?> getUserInfo(@PathVariable("user_id") long userId) {
		User user = userService.getUser(userId);
		List<Team> teams = teamService.getTeamsOfUser(user);
		return ApiResponse.withBody(UserInfo.of(user, teams));
	}
}
