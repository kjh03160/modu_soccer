package com.modu.soccer.utils;

import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UserContextUtil {
	public static final ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<>();

	public static User getCurrentUser() {
		if (USER_THREAD_LOCAL.get() != null) {
			return UserContextUtil.USER_THREAD_LOCAL.get();
		}
		throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
	}

	public static void setUser(User user) {
		UserContextUtil.USER_THREAD_LOCAL.set(user);
	}

	public static void clear() {
		UserContextUtil.USER_THREAD_LOCAL.remove();
	}
}
