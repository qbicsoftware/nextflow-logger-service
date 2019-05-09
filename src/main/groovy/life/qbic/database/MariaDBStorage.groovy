package life.qbic.database

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import life.qbic.micronaututils.QBiCDataSource
import life.qbic.nextflow.WeblogMessage
import life.qbic.nextflow.weblog.MetaData
import life.qbic.nextflow.weblog.RunInfo
import life.qbic.nextflow.weblog.Trace

import javax.inject.Inject
import javax.inject.Singleton
import java.sql.Clob
import java.sql.Connection

@Singleton
class MariaDBStorage implements WeblogStorage{

    private QBiCDataSource dataSource

    private Sql sql

    @Inject MariaDBStorage(QBiCDataSource dataSource) {
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

    private static Trace convertRowResultToTrace(GroovyRowResult row) {
        return new Trace(["task_id": row.get('TASKID'),
            "start": row.get('STARTTIME'),
            "submission": row.get('SUBMISSIONTIME'),
            "name": row.get('NAME'),
            "status": row.get('STATUS'),
            "exit": row.get('EXIT'),
            "attempt": row.get('ATTEMPT'),
            "memory": row.get('MEMORY'),
            "duration": row.get('DURATION'),
            "cpus": row.get('CPUS'),
            "queue": row.get('QUEUE')])
    }

    List<WeblogMessage> findRunWithRunId(String runId) {
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
        final def statement = "SELECT * FROM WORKFLOWS.RUNS WHERE RUNID=${runId};"
        return sql.rows(statement)
    }

    private static WeblogMessage convertRowResultToWeblog(GroovyRowResult rowResult) {
        RunInfo info = new RunInfo()
        info.id = rowResult.get("RUNID")
        info.status = rowResult.get("LASTEVENT" )
        info.name = rowResult.get("NAME")
        info.time = rowResult.get("LASTRECORD")

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
        println "pk: $primaryKeyRun"
        insertTraceInfo(message.trace, primaryKeyRun)
        insertMetadataInfo(message.metadata, primaryKeyRun)
    }

    private void insertMetadataInfo(MetaData metaData, Integer primaryKeyRun) {
        if( metaData == new MetaData() ) {
            return
        }
        sql.execute("""insert into WORKFLOWS.METADATA (runId,
            startTime, parameters, workDir, container, user, manifest,
            revision, duration, success, resume, nextflowVersion, exitStatus, errorMessage) 
            values (
                $primaryKeyRun,
                ${metaData.workflow.'start'},
                ${JsonOutput.toJson(metaData.params)},
                ${metaData.workflow.'workDir'},
                ${metaData.workflow.'container'},
                ${metaData.workflow.'userName'},
                ${JsonOutput.toJson(metaData.workflow.'manifest')},
                ${metaData.workflow.'revision'},
                ${metaData.workflow.'duration'},
                ${metaData.workflow.'success'},
                ${metaData.workflow.'resume'},
                ${metaData.workflow.'nextflow'.'version'},
                ${metaData.workflow.'exitStatus'},
                ${metaData.workflow.'errorMessage'}
            );""")
    }

    private Integer storeRunInfo(RunInfo runInfo) {
        def primaryKey
        if( tryToFindWeblogEntryWithRunId(runInfo.id) ) {
            primaryKey = updateWeblogRunInfo(runInfo)
        } else {
            primaryKey = insertWeblogRunInfo(runInfo)
        }
        println primaryKey
        return primaryKey
    }

    private Integer updateWeblogRunInfo(RunInfo runInfo){
        //TODO update status of a Nextflow run info
    }

    private Integer insertWeblogRunInfo(RunInfo runInfo) {
        sql.execute("""insert into WORKFLOWS.RUNS (runId, name, lastEvent, lastRecord) values \
            ($runInfo.id,
            $runInfo.name,
            ${runInfo.event.toString()},
            ${runInfo.time.toTimestamp()});""")
        def result = tryToFindWeblogEntryWithRunId(runInfo.id)
        if( !result ) {
            throw new WeblogStorageException("Insertion went wrong")
        }
        return result[0].get('id') as Integer

    }

    private void insertTraceInfo(Trace trace, Integer primaryKeyRun) {
        if( trace == new Trace() )
            return
        sql.execute("""insert into WORKFLOWS.TRACES (taskId, runId, startTime, submissionTime, name, status, exit, attempt, memory, cpus, queue, duration) values \
            (${trace.'task_id'},
            $primaryKeyRun,
            ${trace.'start'},
            ${trace.'submission'},
            ${trace.'name'},
            ${trace.'status'},
            ${trace.'exit'},
            ${trace.'attempt'},
            ${trace.'memory'},
            ${trace.'cpus'},
            ${trace.'queue'},
            ${trace.'duration'});""")
    }

    List<MetaData> findMetadataForRunWithId(String id) {
        sql = new Sql(dataSource.connection)
        try {
            def result = tryToFetchMetadataForRun(id)
            return result
        } catch (Exception e) {
            throw new WeblogStorageException("Could not retrieve metadata information for run with id: $id", e)
        }
    }

    private List<MetaData> tryToFetchMetadataForRun(String runId) {
        def resultRows = tryToFindWeblogEntryWithRunId(runId)
        if( resultRows.size() > 1 ) {
            throw new WeblogStorageException("More than one run found for id $runId!. Expected unique result.")
        }
        def primaryKeyRun = resultRows.get(0)["ID"] as Integer
        println primaryKeyRun
        return findMetadataForRunWithForeignKey(primaryKeyRun)
    }

    private List<MetaData> findMetadataForRunWithForeignKey(Integer key) {
        def result = sql.rows("SELECT * FROM WORKFLOWS.METADATA WHERE RUNID=$key;")
        println result
        List<MetaData> metadata = result.collect{ convertRowResultToMetadata(it) }
        return metadata
    }

    private static MetaData convertRowResultToMetadata(GroovyRowResult rowResult) {
        def slurper = new JsonSlurper()
        def workflow = [
                'start': rowResult.get('STARTTIME'),
                'workDir': rowResult.get('WORKDIR'),
                'container': rowResult.get('CONTAINER'),
                'userName': rowResult.get('USER'),
                'manifest': slurper.parseText(parseClob(rowResult.get('MANIFEST') as Clob)),
                'revision': rowResult.get('REVISION'),
                'duration': rowResult.get('DURATION'),
                'success': rowResult.get('SUCCESS'),
                'resume': rowResult.get('RESUME'),
                'nextflow': ['version': rowResult.get('NEXTFLOWVERSION')],
                'exitStatus': rowResult.get('EXITSTATUS'),
                'errorMessage': rowResult.get('ERRORMESSAGE')
        ]


        return new MetaData([
                'params': slurper.parseText(parseClob(rowResult.get('PARAMETERS') as Clob)),
                'workflow': workflow
        ])
    }

    private static String parseClob(Clob clob) {
        Reader reader = clob.getCharacterStream()
        return reader.getText()
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
