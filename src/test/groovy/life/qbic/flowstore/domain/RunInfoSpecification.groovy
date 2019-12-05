package life.qbic.flowstore.domain

import groovy.json.JsonSlurper
import life.qbic.datamodel.workflows.RunInfo
import spock.lang.Specification

class RunInfoSpecification extends Specification {

    def 'create run info from map'() {

        given :
        def fakeWeblogPayload = """
                {
                    "runName": "awesomerun",
                    "runId": "1234-1234",
                    "event": "started",
                    "utcTime": "2018-10-07T11:45:30Z"
                }
                """.stripIndent()

        when :
        def slurper = new JsonSlurper()
        def map = slurper.parseText(fakeWeblogPayload) as Map

        then :
        RunInfo info = new RunInfo(map)
        assert info.name == "awesomerun"
        assert info.id == "1234-1234"
        assert info.event == NextflowEventType.STARTED




    }

}
