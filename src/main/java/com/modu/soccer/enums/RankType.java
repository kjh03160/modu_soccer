package com.modu.soccer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum RankType {
	GOAL,
	ASSIST
	;

	@JsonCreator
	public static RankType from(String symbol) {
		return RankType.valueOf(symbol.toUpperCase());
	}

}