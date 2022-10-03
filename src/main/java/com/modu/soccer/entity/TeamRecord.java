package com.modu.soccer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_records")
@NoArgsConstructor
@Getter
public class TeamRecord extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;
	@Column(nullable = false)
	private Integer win = 0;
	@Column(nullable = false)
	private Integer lose = 0;
	@Column(nullable = false)
	private Integer draw = 0;

	public TeamRecord(Team team) {
		this.team = team;
	}
}
