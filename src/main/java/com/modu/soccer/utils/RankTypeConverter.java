package com.modu.soccer.utils;

import com.modu.soccer.enums.RankType;
import org.springframework.core.convert.converter.Converter;

public class RankTypeConverter implements Converter<String, RankType> {
	@Override
	public RankType convert(String source) {
		return RankType.valueOf(source.toUpperCase());
	}
}
