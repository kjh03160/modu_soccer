package com.modu.soccer.entity;

import java.sql.Time;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Hibernate;

import com.modu.soccer.enums.Position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
	name = "quarter_participations",
	indexes = {
		@Index(name = "idx_in_user_id", columnList = "in_user_id"),
		@Index(name = "idx_out_user_id", columnList = "out_user_id"),
		@Index(name = "idx_team_id", columnList = "team_id"),
		@Index(name = "idx_quarter_id", columnList = "quarter_id"),
	}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuarterParticipation extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "in_user_id")
	private User inUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "out_user_id")
	private User outUser;

	@Column(name = "in_user_name")
	private String inUserName;

	@Column(name = "out_user_name")
	private String outUserName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quarter_id")
	private Quarter quarter;
	private Time eventTime;

	@Column(name = "position")
	@Enumerated(EnumType.STRING)
	private Position position;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		QuarterParticipation that = (QuarterParticipation)o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
