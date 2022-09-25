import spock.lang.Specification

class SpockTest extends Specification {
    def "spock test build"() {
        given:
        var value = 1
        List<Integer> list = new ArrayList<>();

        when:
        list.add(value)

        then:
        value == list.get(0)
    }
}