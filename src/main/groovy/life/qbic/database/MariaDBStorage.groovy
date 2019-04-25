package life.qbic.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import life.qbic.nextflow.WeblogMessage
import life.qbic.nextflow.weblog.MetaData
import life.qbic.nextflow.weblog.RunInfo
import life.qbic.nextflow.weblog.Trace

import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource
import java.sql.Connection

@Singleton
class MariaDBStorage implements WeblogStorage{

    private DataSource dataSource

    @Inject MariaDBStorage(DataSource dataSource) {
        this.dataSource = dataSource
    }

    List<WeblogMessage> findWeblogEntryWithRunId(String runId) {
        final def sql = new Sql(dataSource.connection)
        try {
            def weblogMessage = tryToFindWeblogEntryWithRunId(runId, sql)
            sql.close()
            return weblogMessage
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not query weblog message!", e)
        }
    }

    private static List<WeblogMessage> tryToFindWeblogEntryWithRunId(String runId, Sql sql) {
        final def query = "SELECT * FROM WORKFLOWS.RUNS WHERE RUNID=${runId};"
        def rowResult = sql.rows(query)
        def weblogMessages = new LinkedList<WeblogMessage>()
        rowResult.each {
            def weblog = convertRowResultToWeblog(it)
            weblogMessages.add(weblog)
        }
        return weblogMessages
    }

    private static WeblogMessage convertRowResultToWeblog(GroovyRowResult rowResult) {
        RunInfo info = new RunInfo()
        info.id = rowResult.get("RUNID") as String

        return WeblogMessage.withRunInfo(info)
    }

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException{
        final def sql = new Sql(dataSource.connection)
        try {
            tryToStoreWeblogMessage(message, sql)
            sql.close()
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not store weblog message: $message", e)
        }
    }

    private static void tryToStoreWeblogMessage(WeblogMessage message, Sql sql) {
        insertRunInfo(message.runInfo, sql)
        insertTraceInfo(message.trace, sql)
        insertMetadataInfo(message.metadata, sql)
    }

    private static void insertMetadataInfo(MetaData metaData, Sql sql) {
        //TODO Implement metadata insertion
    }


    private static void insertRunInfo(RunInfo runInfo, Sql sql) {
        sql.execute("""insert into WORKFLOWS.RUNS (runId) values \
            ($runInfo.id);""")
    }

    private static void insertTraceInfo(Trace trace, Sql sql) {
        //TODO Implement trace info insertion
    }
}


@Requires(env="test")
@Requires(property="database.schema-uri")
@Singleton
class DatabaseInit implements BeanCreatedEventListener<WeblogStorage> {

    String schemaUri

    DatabaseInit(@Property(name='database.schema-uri') schemaUri) {
        this.schemaUri = schemaUri
    }

    WeblogStorage onCreated(BeanCreatedEvent<WeblogStorage> event) {
        def sqlStatement = new File(schemaUri).text
        MariaDBStorage storage = event.bean as MariaDBStorage
        setupDatabase(storage.dataSource.connection, sqlStatement)
        return event.bean
    }

    private static setupDatabase(Connection connection, String sqlStatement) {
        Sql sql = new Sql(connection)
        sql.execute(sqlStatement)
    }
}
