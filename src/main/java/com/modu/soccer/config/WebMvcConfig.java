package com.modu.soccer.config;

import com.modu.soccer.jwt.JwtAuthenticateFilter;
import com.modu.soccer.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
	private final JwtProvider jwtProvider;

	@Bean
	public FilterRegistrationBean filterRegistration() {
		FilterRegistrationBean<JwtAuthenticateFilter> authenticateFilter = new FilterRegistrationBean<>(
			new JwtAuthenticateFilter(jwtProvider));
		authenticateFilter.setOrder(1);
		authenticateFilter.addUrlPatterns("/api/v1/*");
		return authenticateFilter;
	}
}
