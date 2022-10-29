package com.modu.soccer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modu.soccer.enums.FormationName;
import com.modu.soccer.enums.Position;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		private Map<String, MemberInfo> memberInfo = Map.of();

		public String toJsonString() {
			ObjectMapper objectMapper = new ObjectMapper();
			try{
				return objectMapper.writeValueAsString(this);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new CustomException(ErrorCode.UNKNOWN_ERROR);
			}
		}
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
