package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.QuarterRepository
import spock.lang.Specification

class QuarterServiceTest extends Specification {
    private QuarterRepository quarterRepository = Mock()
    private TeamRecordService teamRecordService = Mock()
    private QuarterService service;

    def setup() {
        service = new QuarterService(quarterRepository, teamRecordService)
    }

    def "createQuarter"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 2, 1)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)

        1 * teamRecordService.updateTeamRecord(teamA.getId(), teamB.getId(), 1)
        1 * quarterRepository.save(_)

        when:
        service.createQuarterOfMatch(match, request)

        then:
        noExceptionThrown()
    }

    def "getQuartersOfMatch"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)
        quarterRepository.findByMatch(match) >> List.of(quarter)

        when:
        def result = service.getQuartersOfMatch(match)

        then:
        noExceptionThrown()
        result.size() == 1
    }

    def "getQuarterInfoOfMatch"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)
        quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)

        when:
        def result = service.getQuarterInfoOfMatch(match, quarter.getId())

        then:
        noExceptionThrown()
        result.getId() == quarter.getId()
        result.getMatch().getId() == match.getId()
    }

    def "getQuarterInfoOfMatch - not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)
        quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.empty()

        when:
        def result = service.getQuarterInfoOfMatch(match, quarter.getId())

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }
}
