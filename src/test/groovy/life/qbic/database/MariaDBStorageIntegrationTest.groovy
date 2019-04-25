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

    @Shared WeblogMessage message

    def setupSpec() {
        message = WeblogMessage.createFromJson(createTestWeblogJsonPayload())
    }

    static String createTestWeblogJsonPayload(){
        def payload = """{   
                    "runName": "awesomerun",
                    "runId": "1234-1234",
                    "event": "started"
                }
                """.stripIndent()
        return payload
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
        storage.storeWeblogMessage(message)
        def weblogEntryList = storage.findWeblogEntryWithRunId(message.runInfo.id)

        then:
        assert weblogEntryList.size() == 1
        assert weblogEntryList[0].runInfo.id == message.runInfo.id
    }

}

