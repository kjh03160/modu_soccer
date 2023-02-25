package com.modu.soccer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;

@Repository
public interface QuarterParticipationRepository extends JpaRepository<QuarterParticipation, Long> {
	List<QuarterParticipation> findAllByQuarter(Quarter quarter);
}
