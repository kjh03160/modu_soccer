package com.modu.soccer.repository

import com.modu.soccer.TestUtil
import com.modu.soccer.entity.Match
import com.modu.soccer.entity.Quarter
import com.modu.soccer.enums.FormationName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@DataJpaTest
class QuarterRepositoryTest extends Specification {
    @Autowired
    private QuarterRepository repository
    @Autowired
    private MatchRepository matchRepository
    @Autowired
    private TeamMemberRepository memberRepository
    @Autowired
    private UserRepository userRepository
    @Autowired
    private TeamRepository teamRepository
    @Autowired
    private TeamRecordRepository teamRecordRepository
    @PersistenceContext
    private EntityManager entityManager

    private Match match
    private Quarter quarter

    def setup() {
        def user = TestUtil.getUser(null, "email")
        def team1 = TestUtil.getTeam(null, "team1", user)
        def team1Member = TestUtil.getTeamMember(null, user, team1)
        def team2 = TestUtil.getTeam(null, "team2", user)
        def team1Record = TestUtil.getTeamRecord(team1)
        def team2Record = TestUtil.getTeamRecord(team2)
        team1.setRecord(team1Record)
        team2.setRecord(team2Record)

        userRepository.save(user)
        teamRepository.saveAll(List.of(team1, team2))
        teamRecordRepository.saveAll(List.of(team1Record, team2Record))
        memberRepository.save(team1Member)
        def m = TestUtil.getMatch(null, team1, team2, user)
        this.match = matchRepository.save(m)

        quarter = TestUtil.getQuarter(null, this.match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        quarter.setTeamA(team1)
        quarter.setTeamB(team2)
        repository.save(quarter)
        def quarter2 = TestUtil.getQuarter(null, this.match, FormationName.FORMATION_1, FormationName.FORMATION_2, 2, 2, 3)
        quarter2.setTeamA(team1)
        quarter2.setTeamB(team2)
        repository.save(quarter2)

        entityManager.clear()
    }

    def cleanup() {
        repository.deleteAll()
    }

    def "findByMatch"() {
        given:
        def m = matchRepository.getReferenceById(this.match.getId())

        when:
        def quarters = repository.findByMatch(m)

        then:
        noExceptionThrown()
        quarters.size() == 2
        quarters.get(0).getQuarter() == 1
        quarters.get(0).getMatch().getId() == match.getId()
        quarters.get(0).getTeamAScore() == 2
        quarters.get(0).getTeamBScore() == 1
        quarters.get(1).getQuarter() == 2
        quarters.get(1).getMatch().getId() == match.getId()
        quarters.get(1).getTeamAScore() == 2
        quarters.get(1).getTeamBScore() == 3
    }

    def "findByMatch - 없는 매치"() {
        given:
        def m = matchRepository.getReferenceById(10000l)

        when:
        def quarters = repository.findByMatch(m)

        then:
        noExceptionThrown()
        quarters.size() == 0
    }

    def "findByIdAndMatch"() {
        given:
        def m = matchRepository.getReferenceById(this.match.getId())

        when:
        def result = repository.findByIdAndMatch(quarter.getId(), m)

        then:
        noExceptionThrown()
        result.isPresent()
        result.get().getId() == quarter.getId()
        result.get().getMatch().getId() == match.getId()
    }

    def "findByIdAndMatch - 없는 쿼터 id"() {
        given:
        def m = matchRepository.getReferenceById(this.match.getId())

        when:
        def result = repository.findByIdAndMatch(100l, m)

        then:
        noExceptionThrown()
        result.isEmpty()
    }
}
