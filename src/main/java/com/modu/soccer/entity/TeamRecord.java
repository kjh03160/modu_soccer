package com.modu.soccer.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Entity
@Table(
	name = "team_records",
	indexes = {
		@Index(name = "idx_team_id", columnList = "team_id", unique = true)
	}
)
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
	@Column(nullable = false, name = "win_rate")
	private Double winRate = 0.0d;
	@Column(nullable = false)
	private Integer goals = 0;
	@Column(nullable = false, name = "lost_goals")
	private Integer lostGoals = 0;

	public TeamRecord(Team team) {
		this.team = team;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
			o)) {
			return false;
		}
		TeamRecord that = (TeamRecord) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void updateRecord(Integer win, Integer draw, Integer lose, Integer goals, Integer lostGoals) {
		this.win = this.win + win;
		this.draw = this.draw + draw;
		this.lose = this.win + lose;
		this.goals = this.goals + goals;
		this.lostGoals = this.lostGoals + lostGoals;
		this.calculateWinRate();
	}

	private void calculateWinRate() {
		int total = this.getWin() + this.getLose() + this.getDraw();
		if (total == 0) {
			this.winRate = (double) 0;
		}
		this.winRate = this.getWin() / (double) total;
	}
}
