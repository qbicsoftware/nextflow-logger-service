package life.qbic.weblog

import life.qbic.weblog.nextflow.RunInfo
import spock.lang.Specification

class NextflowWeblogSpecification extends Specification {

    def "create run info only weblog message"() {
        given :
        def runInfoOnly = ["runName": "testRun", "runId": "1234", "event": "started", "utcTime": "2018-10-07T11:42:08Z"]


        when :
        RunInfo runInfo = new RunInfo(runInfoOnly)
        NextflowWeblogMessage message = NextflowWeblogMessage.withRunInfo(runInfo)

        then:
        assert message.runInfo == runInfo

    }


}
