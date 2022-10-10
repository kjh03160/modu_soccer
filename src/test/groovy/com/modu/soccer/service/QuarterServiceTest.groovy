package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.MatchRepository
import com.modu.soccer.repository.QuarterRepository
import spock.lang.Specification

class QuarterServiceTest extends Specification {
    private QuarterRepository quarterRepository = Mock()
    private MatchRepository matchRepository = Mock()
    private TeamRecordService teamRecordService = Mock()
    private QuarterService service;

    def setup() {
        service = new QuarterService(quarterRepository, matchRepository, teamRecordService)
    }

    def "createQuarter"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 2, 1)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)

        1 * matchRepository.findById(match.getId()) >> Optional.of(match)
        1 * teamRecordService.updateTeamRecord(teamA.getId(), teamB.getId(), 1)
        1 * quarterRepository.save(_)

        when:
        service.createQuarter(match.getId(), request)

        then:
        noExceptionThrown()
    }

    def "createQuarter - match not found"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 2, 1)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)

        1 * matchRepository.findById(match.getId()) >> Optional.empty()
        0 * teamRecordService.updateTeamRecord(_, _, _)
        0 * quarterRepository.save(_)

        when:
        service.createQuarter(match.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }
}
