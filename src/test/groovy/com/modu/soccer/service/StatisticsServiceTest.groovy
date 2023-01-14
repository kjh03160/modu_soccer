package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.enums.StatisticsType
import com.modu.soccer.repository.AttackPointRepository
import com.modu.soccer.repository.UserRepository
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class StatisticsServiceTest extends Specification {
    private UserRepository userRepository = Mock()
    private AttackPointRepository attackPointRepository = Mock()
    private StatisticsService service

    def setup() {
        service = new StatisticsService(userRepository, attackPointRepository)
    }

    def "getTopMembers - #type"() {
        given:
        def page = PageRequest.of(0, 5)
        def user = TestUtil.getUser(1l, "email")
        def user2 = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", user)
        def records = [TestUtil.getSoloRecordView(user, 2), TestUtil.getSoloRecordView(user2, 1)]

        if (type == StatisticsType.GOAL || type == StatisticsType.ASSIST) {
            1 * attackPointRepository.countAttackPointsByTeamIdAndType(team.getId(), type, page.getPageSize(), page.getOffset()) >> records
        } else if (type == StatisticsType.ATTACK_POINT){
            1 * attackPointRepository.countAttackPointsByTeamIdAndUserId(team.getId(), page.getPageSize(), page.getOffset()) >> records
        }
        1 * userRepository.findAllById(records.stream().map(r -> {return r.getUserId()}).toList()) >> [user, user2]

        when:
        def result = service.getTopMembers(page, team, type)

        then:
        noExceptionThrown()
        result.size() == 2
        result.get(0).getUser().getUserId() == user.getId()
        result.get(0).getCount() == 2
        result.get(1).getUser().getUserId() == user2.getId()
        result.get(1).getCount() == 1

        where:
        type << [StatisticsType.ASSIST, StatisticsType.GOAL, StatisticsType.ATTACK_POINT]
    }

    def "getTopDuoMembers"() {
        given:
        def page = PageRequest.of(0, 5)
        def user = TestUtil.getUser(1l, "email")
        def user2 = TestUtil.getUser(2l, "email2")
        def user3 = TestUtil.getUser(3l, "email3")
        def team = TestUtil.getTeam(1l, "team", user)
        def records = [TestUtil.getDuoRecordView(user, user2, 2), TestUtil.getDuoRecordView(user2, user3, 1)]

        1 * attackPointRepository.countDuoAttackPointsByTeamIdAndGoal(team.getId(), page.getPageSize(), page.getOffset()) >> records
        1 * userRepository.findAllById(
                records.stream()
                        .map(r -> {return Arrays.asList(r.getUserId1(), r.getUserId2())})
                        .flatMap(Collection::stream).toList()
        ) >> [user, user2, user3]

        when:
        def result = service.getTopDuoMembers(page, team)

        then:
        noExceptionThrown()
        result.size() == 2
        result.get(0).getUser1().getUserId() == user.getId()
        result.get(0).getUser2().getUserId() == user2.getId()
        result.get(0).getCount() == 2
        result.get(1).getUser1().getUserId() == user2.getId()
        result.get(1).getUser2().getUserId() == user3.getId()
        result.get(1).getCount() == 1

        where:
        type << [StatisticsType.ASSIST, StatisticsType.GOAL, StatisticsType.ATTACK_POINT]
    }
}
