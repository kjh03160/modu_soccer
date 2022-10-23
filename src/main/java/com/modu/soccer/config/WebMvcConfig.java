package com.modu.soccer.config;

import com.modu.soccer.filter.LoggingFilter;
import com.modu.soccer.jwt.JwtInterceptor;
import com.modu.soccer.jwt.JwtProvider;
import com.modu.soccer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;

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
			.excludePathPatterns("**/favicon.ico", "/**/error", "/error-page/**",
				"/api/v1/oauth/**", "/api/v1/user/token",
				"/api/health");
	}

	@Bean
	public JwtInterceptor jwtInterceptor() {
		return new JwtInterceptor(userRepository, jwtProvider);
	}
}
