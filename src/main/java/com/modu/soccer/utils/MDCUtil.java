package com.modu.soccer.utils;

import com.modu.soccer.enums.MDCKey;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class MDCUtil {

	public static Long getUserIdFromMDC() {
		String userId = MDC.get(MDCKey.USER_ID.getKey());
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
		}
		if (!StringUtils.isNumeric(userId)) {
			throw new IllegalArgumentException(String.format("%s is not number", userId));
		}
		return Long.valueOf(userId);
	}
}
