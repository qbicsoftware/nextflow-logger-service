package life.qbic.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import io.micronaut.test.annotation.*
import life.qbic.nextflow.WeblogMessage
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.sql.Connection

@MicronautTest(environments=['test'])
class MariaDBStorageIntegrationTest extends Specification {

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

    def "store weblog message"() {
        when:
        storage.storeWeblogMessage(messageWithTrace)
        def weblogEntryList = storage.findWeblogEntryWithRunId(messageWithTrace.runInfo.id)
        def traces = storage.findTracesForRunWithId(messageWithTrace.runInfo.id)

        then:
        assert weblogEntryList.size() == 1
        assert weblogEntryList[0].runInfo.id == messageWithTrace.runInfo.id
        assert traces.size() == 1
        assert traces[0].getProperty('task_id') == messageWithTrace.trace.getProperty('task_id')
        assert traces[0].getProperty('start') == messageWithTrace.trace.getProperty('start')
        assert traces[0].getProperty('submission') == messageWithTrace.trace.getProperty('submission')
        assert traces[0].getProperty('name') == messageWithTrace.trace.getProperty('name')
        assert traces[0].getProperty('status') == messageWithTrace.trace.getProperty('status')
        assert traces[0].getProperty('exit') == messageWithTrace.trace.getProperty('exit')
        assert traces[0].getProperty('attempt') == messageWithTrace.trace.getProperty('attempt')
        assert traces[0].getProperty('queue') == messageWithTrace.trace.getProperty('queue')
        assert traces[0].getProperty('memory') == messageWithTrace.trace.getProperty('memory')
        assert traces[0].getProperty('cpus') == messageWithTrace.trace.getProperty('cpus')
        assert traces[0].getProperty('duration') == messageWithTrace.trace.getProperty('duration')
    }

}

