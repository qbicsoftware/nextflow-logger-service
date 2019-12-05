package life.qbic.flowstore.domain

import life.qbic.datamodel.workflows.MetaData
import life.qbic.datamodel.workflows.RunInfo
import life.qbic.datamodel.workflows.Trace
import spock.lang.Specification

class WorkflowSpecification extends Specification {

    def "create run info only weblog message"() {
        given :
        def runInfoOnly = ["runName": "testRun", "runId": "1234", "event": "started", "utcTime": "2018-10-07T11:42:08Z"]


        when :
        RunInfo runInfo = new RunInfo(runInfoOnly)
        Workflow message = Workflow.withRunInfo(runInfo)

        then:
        assert message.runInfo == runInfo
        assert message.trace == new Trace()
        assert message.metadata == new MetaData()

    }

    def "create weblog message with run and trace info"() {
        given :
        def runAndTraceInfo = ["runName": "testRun", "runId": "1234", "event": "started",
                               "utcTime": "2018-10-07T11:42:08Z", "trace": ["task_id": 6, "status": "COMPLETED"]]

        when :
        RunInfo runInfo = new RunInfo(runAndTraceInfo.findAll { k,v -> v != "trace" })
        Trace trace = new Trace(runAndTraceInfo.trace)
        Workflow message = Workflow.withRunAndTraceInfo(runInfo, trace)

        then:
        assert message.runInfo == runInfo
        assert message.trace == trace
        assert message.trace != new Trace()
        assert message.trace.task_id == 6
        assert message.metadata == new MetaData()

    }

    def "create weblog message with run and workflow info"() {
        given :
        def runAndMetadataInfo = ["runName": "testRun", "runId": "1234", "event": "started",
                               "utcTime": "2018-10-07T11:42:08Z",
                               "metadata": ["parameters": ["myParam": 123], "workflow": ["start": "2019-03-27T13:37:14Z"]]]

        when :
        RunInfo runInfo = new RunInfo(runAndMetadataInfo.findAll { k,v -> v != "metadata" })
        MetaData metadata = new MetaData(runAndMetadataInfo.metadata)
        Workflow message = Workflow.withRunAndMetadataInfo(runInfo, metadata)

        then:
        assert message.runInfo == runInfo
        assert message.metadata == metadata
        assert message.metadata.parameters == ["myParam": 123]
        assert message.metadata.workflow.start == "2019-03-27T13:37:14Z"
        assert message.trace == new Trace()
    }

    def "create weblog message from json"() {

        given :
        def fakeWeblogPayload = """
                {
                    "runName": "awesomerun",
                    "runId": "1234-1234",
                    "event": "started",
                    "utcTime": "2018-10-07T11:45:30Z",
                    "metadata": {
                        "parameters": {
                            "myparam": 123
                        }
                    },
                    "trace": {
                        "task_id": 3
                    }
                }
                """.stripIndent()

        when:
        assert fakeWeblogPayload
        Workflow message = Workflow.createFromJson(fakeWeblogPayload)

        then:
        assert fakeWeblogPayload
        assert message.runInfo.name == "awesomerun"
        assert message.metadata instanceof MetaData
        assert message.metadata.parameters instanceof Map
        assert message.trace instanceof Trace
        assert message.trace.getProperty('task_id') == 3
        assert !message.trace.getProperty('notpresent')
    }


}
