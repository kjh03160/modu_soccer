package com.modu.soccer.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(
	name = "quarters"
//	indexes = {
//		@Index(name = "idx_match_id_quarter", columnList = "match_id, quarter", unique = true)
//	}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeDef(name = "json", typeClass = JsonType.class, defaultForType = Formation.class)
public class Quarter extends BaseEntity implements Comparable<Quarter>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_id")
	private Match match;

	@Column(name = "team_a_score")
	private Integer teamAScore;
	@Column(name = "team_b_score")
	private Integer teamBScore;

	@Type(type = "json")
	@Column(columnDefinition = "JSON")
	private Formation formation;
	@Column(nullable = false)
	private Integer quarter;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
			o)) {
			return false;
		}
		Quarter quarter = (Quarter) o;
		return id != null && Objects.equals(id, quarter.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

	@Override
	public int compareTo(Quarter o) {
		return this.quarter.compareTo(o.quarter);
	}
}
