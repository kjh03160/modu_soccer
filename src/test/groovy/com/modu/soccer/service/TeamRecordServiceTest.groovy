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
        service.updateTeamRecord(teamA.getId(), teamB.getId(), teamAScore, teamBScore, false)

        then:
        noExceptionThrown()
        if (teamAScore > teamBScore) {
            teamARecord.getWin() == 1
            teamARecord.getDraw() == 0
            teamARecord.getLose() == 0
            teamARecord.getGoals() == teamAScore
            teamARecord.getLostGoals() == teamBScore
            teamARecord.getWinRate() == 1.0

            teamBRecord.getWin() == 0
            teamBRecord.getDraw() == 0
            teamBRecord.getLose() == 1
            teamBRecord.getGoals() == teamBScore
            teamBRecord.getLostGoals() == teamAScore
            teamARecord.getWinRate() == 0.0
        } else if (teamAScore == teamBScore) {
            teamARecord.getWin() == 0
            teamARecord.getDraw() == 1
            teamARecord.getLose() == 0
            teamARecord.getGoals() == teamAScore
            teamARecord.getLostGoals() == teamBScore
            teamARecord.getWinRate() == 0.0

            teamBRecord.getWin() == 0
            teamBRecord.getDraw() == 1
            teamBRecord.getLose() == 0
            teamBRecord.getGoals() == teamBScore
            teamBRecord.getLostGoals() == teamAScore
            teamARecord.getWinRate() == 0.0
        } else {
            teamARecord.getWin() == 0
            teamARecord.getDraw() == 0
            teamARecord.getLose() == 1
            teamARecord.getGoals() == teamAScore
            teamARecord.getLostGoals() == teamBScore
            teamARecord.getWinRate() == 0.0

            teamBRecord.getWin() == 1
            teamBRecord.getDraw() == 0
            teamBRecord.getLose() == 0
            teamBRecord.getGoals() == teamBScore
            teamBRecord.getLostGoals() == teamAScore
            teamARecord.getWinRate() == 1.0
        }

        where:
        teamAScore | teamBScore
        1          | 0
        0          | 1
        1          | 1
    }

    @Unroll
    def "updateTeamRecord 쿼터 삭제 시 레코드 복구 teamAScore: #teamAScore, teamBScore: #teamBScore"() {
        given:
        // win, draw, lose, goals, lostGoals
        def teamAPrev = [1, 1, 1, 1, 1]
        def teamBPrev = [1, 1, 1, 1, 1]

        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamARecord = TestUtil.getTeamRecord(teamA)
        teamARecord.win = teamAPrev[0]
        teamARecord.draw = teamAPrev[1]
        teamARecord.lose = teamAPrev[2]
        teamARecord.goals = teamAPrev[3]
        teamARecord.lostGoals = teamAPrev[4]
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def teamBRecord = TestUtil.getTeamRecord(teamB)
        teamBRecord.win = teamBPrev[0]
        teamBRecord.draw = teamBPrev[1]
        teamBRecord.lose = teamBPrev[2]
        teamBRecord.goals = teamBPrev[3]
        teamBRecord.lostGoals = teamBPrev[4]

        1 * recordRepository.findByTeamId(teamA.getId()) >> Optional.of(teamARecord)
        1 * recordRepository.findByTeamId(teamB.getId()) >> Optional.of(teamBRecord)

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), teamAScore, teamBScore, true)

        then:
        noExceptionThrown()
        if (teamAScore > teamBScore) {
            teamARecord.getWin() == teamAPrev[0] - 1
            teamARecord.getDraw() == teamAPrev[1]
            teamARecord.getLose() == teamAPrev[2]
            teamARecord.getGoals() == teamAPrev[3] - teamAScore
            teamARecord.getLostGoals() == teamAPrev[4] - teamBScore
            teamARecord.getWinRate() == teamARecord.getWin() / (double) (teamARecord.getWin() + teamAPrev[1] + teamAPrev[2])

            teamBRecord.getWin() == teamBPrev[0]
            teamBRecord.getDraw() == teamBPrev[1]
            teamBRecord.getLose() == teamBPrev[2] - 1
            teamBRecord.getGoals() == teamBPrev[3] - teamAScore
            teamBRecord.getLostGoals() == teamBPrev[4] - teamBScore
            teamBRecord.getWinRate() == teamBRecord.getWin() / (double) (teamBPrev[0] + teamBPrev[1] + teamBRecord.getLose())
        } else if (teamAScore == teamBScore) {
            teamARecord.getWin() == teamAPrev[0]
            teamARecord.getDraw() == teamAPrev[1] - 1
            teamARecord.getLose() == teamAPrev[2]
            teamARecord.getGoals() == teamAPrev[3] - teamAScore
            teamARecord.getLostGoals() == teamAPrev[4] - teamBScore
            teamARecord.getWinRate() == teamARecord.getWin() / (double) (teamAPrev[0] + teamARecord.getDraw() + teamAPrev[2])

            teamBRecord.getWin() == teamBPrev[0]
            teamBRecord.getDraw() == teamBPrev[1] - 1
            teamBRecord.getLose() == teamBPrev[2]
            teamBRecord.getGoals() == teamBPrev[3] - teamAScore
            teamBRecord.getLostGoals() == teamBPrev[4] - teamBScore
            teamBRecord.getWinRate() == teamBRecord.getWin() / (double) (teamBPrev[0] + teamBRecord.getDraw() + teamBPrev[2])
        } else {
            teamARecord.getWin() == teamAPrev[0]
            teamARecord.getDraw() == teamAPrev[1]
            teamARecord.getLose() == teamAPrev[2] - 1
            teamARecord.getGoals() == teamAPrev[3] - teamAScore
            teamARecord.getLostGoals() == teamAPrev[4] - teamBScore
            teamARecord.getWinRate() == teamARecord.getWin() / (double) (teamAPrev[0] + teamAPrev[1] + teamARecord.getLose())

            teamBRecord.getWin() == teamBPrev[0] - 1
            teamBRecord.getDraw() == teamBPrev[1]
            teamBRecord.getLose() == teamBPrev[2]
            teamBRecord.getGoals() == teamBPrev[3] - teamAScore
            teamBRecord.getLostGoals() == teamBPrev[4] - teamBScore
            teamBRecord.getWinRate() == teamBRecord.getWin() / (double) (teamBRecord.getWin() + teamBPrev[1] + teamBPrev[2])
        }

        where:
        teamAScore | teamBScore
        1          | 0
        0          | 1
        1          | 1
    }

    def "updateTeamRecord - teamA not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * recordRepository.findByTeamId(teamA.getId()) >> Optional.empty()

        when:
        service.updateTeamRecord(teamA.getId(), teamB.getId(), 1, 1, false)

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
        service.updateTeamRecord(teamA.getId(), teamB.getId(), 1, 1, false)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

}
