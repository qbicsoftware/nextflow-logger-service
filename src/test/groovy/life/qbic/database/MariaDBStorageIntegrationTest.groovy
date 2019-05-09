package life.qbic.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import io.micronaut.test.annotation.*
import life.qbic.nextflow.WeblogMessage
import life.qbic.nextflow.weblog.MetaData
import life.qbic.nextflow.weblog.Trace
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
    WeblogStorage storage

    @Shared WeblogMessage messageWithTrace

    @Shared WeblogMessage messageWithMetadata

    def setupSpec() {
        messageWithTrace = WeblogMessage.createFromJson(
                new File("src/test/resources/WeblogPayloadWithTrace.json").text
        )
        messageWithMetadata = WeblogMessage.createFromJson(
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
        def weblogEntryList = storage.findRunWithRunId(messageWithTrace.runInfo.id)
        def traces = storage.findTracesForRunWithId(messageWithTrace.runInfo.id)

        then:
        assert weblogEntryList.size() == 1
        assert weblogEntryList[0].runInfo.id == messageWithTrace.runInfo.id
        assert traces.size() == 1
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
        def weblogEntryList = storage.findRunWithRunId(messageWithMetadata.runInfo.id)
        def metadata = storage.findMetadataForRunWithId(messageWithMetadata.runInfo.id)

        then:
        assert weblogEntryList.size() == 1
        assert weblogEntryList[0].runInfo.id == messageWithMetadata.runInfo.id
        assert metadata
        compareMetadata(metadata[0], messageWithMetadata.metadata)
    }

    private compareMetadata(MetaData meta, MetaData otherMeta) {
        metadataFields.each { field ->
            assert meta.getProperty(field) == otherMeta.getProperty(field)
        }
    }

}

