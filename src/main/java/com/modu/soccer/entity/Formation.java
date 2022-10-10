package com.modu.soccer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.enums.FormationName;
import com.modu.soccer.enums.Position;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Formation {
	@JsonProperty("team_a")
	private TeamFormation teamA = new TeamFormation();
	@JsonProperty("team_b")
	private TeamFormation teamB = new TeamFormation();

	public Formation(Team teamA, Team teamB) {
		this.teamA.setTeamId(teamA.getId());
		this.teamB.setTeamId(teamB.getId());
	}

	@Getter
	@Setter
	public static class TeamFormation {
		@JsonProperty("team_id")
		private Long teamId;
		@JsonProperty("formation_name")
		private FormationName formationName;
		@JsonProperty("member_info")
		private Map<String, MemberInfo> memberInfo = Map.of(
			"1", new MemberInfo(),
			"2", new MemberInfo(),
			"3", new MemberInfo(),
			"4", new MemberInfo(),
			"5", new MemberInfo(),
			"6", new MemberInfo(),
			"7", new MemberInfo(),
			"8", new MemberInfo(),
			"9", new MemberInfo()
		);
	}

	@Getter
	@Setter
	public static class MemberInfo {
		@JsonProperty("member_id")
		private Long memberId;
		@JsonProperty("member_name")
		private String memberName;
		@JsonProperty("back_number")
		private Integer backNumber;
		@JsonProperty("position")
		private Position position;
	}
}
