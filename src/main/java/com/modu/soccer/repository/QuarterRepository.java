package com.modu.soccer.repository;

import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuarterRepository extends JpaRepository<Quarter, Long> {
	List<Quarter> findByMatch(Match match);
}
