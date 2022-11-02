package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.TeamRecordRepository
import spock.lang.Specification
import spock.lang.Unroll

class TeamRecordServiceTest extends Specification {
    private TeamRecordRepository recordRepository = Mock()
    private TeamRecordService service

    def setup() {
        service = new TeamRecordService(recordRepository)
    }

    @Unroll
    def "updateTeamRecord teamAScore: #teamAScore, teamBScore: #teamBScore"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamARecord = TestUtil.getTeamRecord(teamA)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def teamBRecord = TestUtil.getTeamRecord(teamB)

        1 * recordRepository.findByTeamId(teamA.getId()) >> Optional.of(teamARecord)
        1 * recordRepository.findByTeamId(teamB.getId()) >> Optional.of(teamBRecord)

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), teamAScore, teamBScore)

        then:
        noExceptionThrown()

        where:
        teamAScore | teamBScore
        1          | 0
        0          | 1
        0          | 0
    }

    def "updateTeamRecord - teamA not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * recordRepository.findByTeamId(teamA.getId()) >> Optional.empty()

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), 1, 1)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "updateTeamRecord - teamB not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamARecord = TestUtil.getTeamRecord(teamA)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * recordRepository.findByTeamId(teamA.getId()) >> Optional.of(teamARecord)
        1 * recordRepository.findByTeamId(teamB.getId()) >> Optional.empty()

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), 1, 1)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

}
