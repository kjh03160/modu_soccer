package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.TeamRecordRepository
import com.modu.soccer.repository.TeamRepository
import spock.lang.Specification
import spock.lang.Unroll

class TeamRecordServiceTest extends Specification {
    private TeamRecordRepository recordRepository = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamRecordService service

    def setup() {
        service = new TeamRecordService(recordRepository, teamRepository)
    }

    @Unroll
    def "updateTeamRecord scoreDiff: #scoreDiff"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * teamRepository.findById(teamA.getId()) >> Optional.of(teamA)
        1 * teamRepository.findById(teamB.getId()) >> Optional.of(teamB)

        if (scoreDiff > 0) {
            1 * recordRepository.updateTeamRecord(teamA, 1, 0, 0)
            1 * recordRepository.updateTeamRecord(teamB, 0, 0, 1)
        } else if (scoreDiff == 0) {
            1 * recordRepository.updateTeamRecord(teamA, 0, 1, 0)
            1 * recordRepository.updateTeamRecord(teamB, 0, 1, 0)
        } else {
            1 * recordRepository.updateTeamRecord(teamA, 0, 0, 1)
            1 * recordRepository.updateTeamRecord(teamB, 1, 0, 0)
        }

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), scoreDiff)

        then:
        noExceptionThrown()

        where:
        scoreDiff << [1, 0, -1]
    }

    def "updateTeamRecord - teamA not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * teamRepository.findById(teamA.getId()) >> Optional.empty()

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), 1)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "updateTeamRecord - teamB not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * teamRepository.findById(teamA.getId()) >> Optional.of(teamA)
        1 * teamRepository.findById(teamB.getId()) >> Optional.empty()

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), 1)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

}
