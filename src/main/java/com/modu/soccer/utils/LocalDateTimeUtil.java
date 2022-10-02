package com.modu.soccer.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class LocalDateTimeUtil {
	private static ZoneId TIMEZONE = ZoneId.of("Asia/Seoul");

	public static LocalDateTime now() {
		return ZonedDateTime.now(TIMEZONE).toLocalDateTime();
	}

	public static Date toDate(LocalDateTime localDateTime) {
		return java.sql.Timestamp.valueOf(localDateTime);
	}

	public static LocalDateTime fromDate(Date date) {
		return date.toInstant().atZone(TIMEZONE).toLocalDateTime();
	}
}
