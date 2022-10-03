package com.modu.soccer.domain;

import com.modu.soccer.entity.TeamRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TeamRecordDto {
	private Integer total;
	private Integer win;
	private Integer lose;
	private Integer draw;
	private Integer winRate;

	public static TeamRecordDto fromEntity(TeamRecord record) {
		return TeamRecordDto.builder()
			.win(record.getWin())
			.draw(record.getDraw())
			.lose(record.getLose())
			.total(record.getWin() + record.getDraw() + record.getLose())
			.winRate(calculateWinRate(record))
			.build();
	}

	private static Integer calculateWinRate(TeamRecord record) {
		int total = record.getWin() + record.getLose() + record.getDraw();
		if (total == 0) {
			return 0;
		}
		float rate = record.getWin() / (float) total;
		return Math.round(rate * 100);
	}
}
