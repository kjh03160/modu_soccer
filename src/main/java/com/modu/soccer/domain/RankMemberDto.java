package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.TeamMember;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class RankMemberDto {
	private Long userId;
	private Long teamId;
	private String name;
	private Integer value;

	public static List<RankMemberDto> convertMapToMembersList(Map<TeamMember, Integer> map) {
		return map.keySet().stream().map(m ->
			RankMemberDto.builder()
				.name(m.getUser().getName())
				.teamId(m.getTeam().getId())
				.userId(m.getUser().getId())
				.value(map.get(m))
				.build()
		).toList();
	}
}
