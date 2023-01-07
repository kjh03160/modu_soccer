package com.modu.soccer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modu.soccer.filter.LoggingFilter;
import com.modu.soccer.jwt.JwtInterceptor;
import com.modu.soccer.jwt.JwtProvider;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.AttackPointTypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;
	private final ObjectMapper mapper;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new AttackPointTypeConverter());
	}

	@Bean
	public FilterRegistrationBean getFilterRegistrationBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean(new LoggingFilter());
		registrationBean.setOrder(Integer.MIN_VALUE);
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtInterceptor())
			.order(1)
			.excludePathPatterns("**/favicon.ico", "/error-page/**",
				"/api/v1/oauth/**", "/api/v1/user/token",
				"/api/health");
	}

	@Bean
	public JwtInterceptor jwtInterceptor() {
		return new JwtInterceptor(userRepository, jwtProvider, mapper);
	}
}
