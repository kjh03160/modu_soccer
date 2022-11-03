package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.TeamRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonNaming(SnakeCaseStrategy.class)
public class TeamRecordDto {
	private Integer total;
	private Integer win;
	private Integer lose;
	private Integer draw;
	private Integer winPercent;
	private Integer goals;
	private Integer lostGoals;

	public static TeamRecordDto fromEntity(TeamRecord record) {
		return TeamRecordDto.builder()
			.win(record.getWin())
			.draw(record.getDraw())
			.lose(record.getLose())
			.total(record.getWin() + record.getDraw() + record.getLose())
			.winPercent((int) Math.round(record.getWinRate() * 100))
			.goals(record.getGoals())
			.lostGoals(record.getLostGoals())
			.build();
	}
}
