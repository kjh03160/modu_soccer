package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuarterDetail {
	@JsonUnwrapped
	private QuarterSummary summary;

	public static QuarterDetail fromMatchAndQuarter(Match match, Quarter quarter) {
		QuarterSummary quarterSummary = QuarterSummary.fromMatchAndQuarter(match, quarter);
		return QuarterDetail.builder()
			.summary(quarterSummary)
			.build();
	}
}
