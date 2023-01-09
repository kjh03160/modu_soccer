package com.modu.soccer.utils;

import com.modu.soccer.enums.AttackPointType;
import org.springframework.core.convert.converter.Converter;

public class AttackPointTypeConverter implements Converter<String, AttackPointType> {

	@Override
	public AttackPointType convert(String source) {
		return AttackPointType.valueOf(source);
	}
}
