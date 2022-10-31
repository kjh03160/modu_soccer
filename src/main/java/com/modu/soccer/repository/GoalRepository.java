package com.modu.soccer.repository;

import com.modu.soccer.entity.Goal;
import com.modu.soccer.entity.Quarter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
	List<Goal> findAllByQuarter(Quarter quarter);
}
