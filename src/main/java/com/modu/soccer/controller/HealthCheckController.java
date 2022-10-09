package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@GetMapping("/api/health")
	public ApiResponse<?> healthCheck() {
		return ApiResponse.ok();
	}
}
