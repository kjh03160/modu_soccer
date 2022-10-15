package com.modu.soccer.enums;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FormationName {
	FORMATION_1("4-4-2"),
	FORMATION_2("4-2-3-1"),
	FORMATION_3("4-3-3"),

	;

	public static final Map<String, FormationName> stringToEnum =
		Stream.of(values()).collect(toMap(FormationName::getFormation, formationName -> formationName));

	private final String formation;

	@JsonCreator
	public static FormationName fromString(String symbol) {
		if (FormationName.stringToEnum.containsKey(symbol)) {
			return FormationName.stringToEnum.get(symbol);
		}
		throw new IllegalArgumentException(String.format("%s is invalid formation", symbol));
	}

	@JsonValue
	public String getFormation() {
		return formation;
	}
}
