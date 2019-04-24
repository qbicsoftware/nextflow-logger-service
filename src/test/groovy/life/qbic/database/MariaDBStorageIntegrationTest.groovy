package life.qbic.database

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import io.micronaut.test.annotation.*
import life.qbic.nextflow.WeblogMessage
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.sql.Connection
import java.sql.ResultSet

@MicronautTest(environments=['test'])
class MariaDBStorageIntegrationTest extends Specification {

    String DATABASE_SCHEMA_FILE

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
        def result = sql.rows ("SELECT column_name FROM information_schema.columns WHERE table_name='WORKFLOWRUNS';")
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
        def weblogEntry = storage.getWeblogEntryById(message.runInfo.runId)

        then:
        assert weblogEntry.runInfo.runId == message.runInfo.runId
    }

}

