package com.modu.soccer.config;

import com.modu.soccer.jwt.JwtInterceptor;
import com.modu.soccer.jwt.JwtProvider;
import com.modu.soccer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtInterceptor())
			.order(1)
			.excludePathPatterns("/api/v1/oauth/**", "/api/v1/user/token");
	}

	@Bean
	public JwtInterceptor jwtInterceptor() {
		return new JwtInterceptor(userRepository, jwtProvider);
	}
}
