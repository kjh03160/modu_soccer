package com.modu.soccer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.modu.soccer.entity.QuarterParticipation;

@Repository
public interface QuarterParticipationRepository extends JpaRepository<QuarterParticipation, Long> {
}
