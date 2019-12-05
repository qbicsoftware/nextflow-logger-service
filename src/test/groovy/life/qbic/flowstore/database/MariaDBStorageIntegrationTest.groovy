package life.qbic.flowstore.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.*
import life.qbic.datamodel.workflows.MetaData
import life.qbic.datamodel.workflows.RunInfo
import life.qbic.datamodel.workflows.Trace
import life.qbic.flowstore.domain.Workflows
import life.qbic.flowstore.domain.Workflow

import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.sql.Connection

@MicronautTest(environments=['test'])
class MariaDBStorageIntegrationTest extends Specification {

    @Shared List<String> traceFields = [
            'task_id',
            'start',
            'name',
            'status',
            'exit',
            'attempt',
            'queue',
            'memory',
            'cpus',
            'duration'
    ]

    @Shared List<String> metadataFields = [
            'params',
            'workflow'
    ]

    @Inject
    Workflows storage

    @Inject
    @Shared
    EmbeddedServer server

    @Shared Workflow messageWithTrace

    @Shared Workflow messageWithMetadata

    def setupSpec() {
        messageWithTrace = Workflow.createFromJson(
                new File("src/test/resources/WeblogPayloadWithTrace.json").text
        )
        messageWithMetadata = Workflow.createFromJson(
                new File("src/test/resources/WeblogPayloadWithMetaData.json").text
        )
    }

    def "confirm that database was setup"() {
        given:
        Connection connection = storage.dataSource.connection

        when:
        List<GroovyRowResult> result = queryColumnNames(connection)

        then:
        assert result
    }

    List<GroovyRowResult> queryColumnNames(Connection connection) {
        Sql sql = new Sql(connection)
        def result = sql.rows ("""SELECT column_name FROM information_schema.columns \
                WHERE table_name='RUNS' OR table_name='TRACES';""")
        return result
    }

    def "confirm that storage connection is alive"() {
        when:
        def dataSource = storage.dataSource

        then:
        assert dataSource
    }

    def "store weblog message with trace"() {
        when:
        storage.storeWeblogMessage(messageWithTrace)
        def runInfoList = storage.findRunWithRunId(messageWithTrace.runInfo.id)
        def traces = storage.findTracesForRunWithId(messageWithTrace.runInfo.id)

        then:
        assert runInfoList.size() == 1
        assert runInfoList[0].id == messageWithTrace.runInfo.id
        assert traces.size() >= 1
        compareTraces(traces[0], messageWithTrace.trace)
    }

    private compareTraces(Trace trace, Trace otherTrace) {
        traceFields.each {field ->
            assert trace.getProperty(field) == otherTrace.getProperty(field)
        }
    }

    def "store weblog message with workflow metadata"() {
        when:
        storage.storeWeblogMessage(messageWithMetadata)
        def runInfoList = storage.findRunWithRunId(messageWithMetadata.runInfo.id)
        def metadata = storage.findMetadataForRunWithId(messageWithMetadata.runInfo.id)

        then:
        assert runInfoList.size() == 1
        assert runInfoList[0].id == messageWithMetadata.runInfo.id
        assert metadata
        compareMetadata(metadata[0], messageWithMetadata.metadata)
    }

    private static void compareMetadata(MetaData meta, MetaData otherMeta) {
        assert meta.'workflow'.'start'.toString() == otherMeta.'workflow'.'start'
        assert meta.'workflow'.'manifest' == otherMeta.'workflow'.'manifest'
    }

    def "update run info when there is already a workflow information in the database"() {
        given:
        RunInfo newInfo = new RunInfo().tap {
            name = messageWithMetadata.runInfo.name
            id = messageWithMetadata.runInfo.id
            status = "completed"
            time = new Date()
        }
        Workflow messageWithUpdate = Workflow.withRunInfo(newInfo)


        when:
        storage.storeWeblogMessage(messageWithMetadata)
        storage.storeWeblogMessage(messageWithUpdate)
        def runInfoList = storage.findRunWithRunId(messageWithMetadata.runInfo.id)

        then:
        assert runInfoList.size() == 1
        assert runInfoList[0].event == newInfo.event
    }

    def cleanupSpec() {
        server.stop()
    }

}

