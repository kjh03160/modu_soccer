package com.modu.soccer.utils;

import com.modu.soccer.enums.StatisticsType;
import org.springframework.core.convert.converter.Converter;

public class StatisticTypeConverter implements Converter<String, StatisticsType> {

	@Override
	public StatisticsType convert(String source) {
		return StatisticsType.valueOf(source.toUpperCase());
	}
}
