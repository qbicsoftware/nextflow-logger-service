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

    private Sql sql

    @Inject MariaDBStorage(DataSource dataSource) {
        this.dataSource = dataSource
    }

    List<Trace> findTracesForRunWithId(String id) {
        sql = new Sql(dataSource.connection)
        try {
            def result = tryToFetchTracesForRun(id)
            return result
        } catch (Exception e) {
            throw new WeblogStorageException("Could not fetch trace information for run with id $id.", e)
        }
    }

    private List<Trace> tryToFetchTracesForRun(String runId) {
        def resultRows = tryToFindWeblogEntryWithRunId(runId)
        if( resultRows.size() > 1 ) {
            throw new WeblogStorageException("More than one run found for id $runId!. Expected unique result.")
        }
        def primaryKeyRun = resultRows.get(0)["ID"] as Integer
        def traces = findTracesForRunWithPrimaryKey(primaryKeyRun)
        return traces
    }

    private List<Trace> findTracesForRunWithPrimaryKey(Integer key) {
        def result = sql.rows("""SELECT * FROM WORKFLOWS.TRACES WHERE RUNID=$key;""")
        List<Trace> traces = result.collect{ convertRowResultToTrace(it) }
        return traces
    }

    private Trace convertRowResultToTrace(GroovyRowResult row) {
        return new Trace(["task_id": row.get('TASKID')])
    }

    List<WeblogMessage> findWeblogEntryWithRunId(String runId) {
        sql = new Sql(dataSource.connection)
        try {
            def result = tryToFindWeblogEntryWithRunId(runId)
            def weblogMessages = result.collect { convertRowResultToWeblog(it) }
            sql.close()
            return weblogMessages
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not query weblog message with run id $runId!", e)
        }
    }

    private List<GroovyRowResult> tryToFindWeblogEntryWithRunId(String runId) {
        final def query = "SELECT * FROM WORKFLOWS.RUNS WHERE RUNID=${runId};"
        def rowResult = sql.rows(query)
        return rowResult
    }

    private static WeblogMessage convertRowResultToWeblog(GroovyRowResult rowResult) {
        RunInfo info = new RunInfo()
        info.id = rowResult.get("RUNID") as String

        return WeblogMessage.withRunInfo(info)
    }

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException{
       this.sql = new Sql(dataSource.connection)
        try {
            tryToStoreWeblogMessage(message)
            sql.close()
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not store weblog message: $message", e)
        }
    }

    private void tryToStoreWeblogMessage(WeblogMessage message) {
        def primaryKeyRun = storeRunInfo(message.runInfo)
        insertTraceInfo(message.trace, primaryKeyRun)
        insertMetadataInfo(message.metadata, primaryKeyRun)
    }

    private void insertMetadataInfo(MetaData metaData, Integer primaryKeyRun) {
        //TODO Implement metadata insertion
    }


    private Integer storeRunInfo(RunInfo runInfo) {
        def primaryKey
        if( tryToFindWeblogEntryWithRunId(runInfo.id) ) {
            primaryKey = updateWeblogRunInfo(runInfo)
        } else {
            primaryKey = insertWeblogRunInfo(runInfo)
        }
        return primaryKey
    }

    private Integer updateWeblogRunInfo(RunInfo runInfo){
        //TODO update status of a Nextflow run info
    }

    private Integer insertWeblogRunInfo(RunInfo runInfo) {
        sql.execute("""insert into WORKFLOWS.RUNS (runId) values \
            ($runInfo.id);""")
        def result = tryToFindWeblogEntryWithRunId(runInfo.id)
        if( !result ) {
            throw new WeblogStorageException("Insertion went wrong")
        }
        return result[0].get('id') as Integer

    }

    private void insertTraceInfo(Trace trace, Integer primaryKeyRun) {
        sql.execute("""insert into WORKFLOWS.TRACES (taskId, runId) values \
            (${trace.'task_id'}, $primaryKeyRun);""")
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
