package life.qbic.database

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Log4j2
import life.qbic.Constants
import life.qbic.service.WeblogStorage
import life.qbic.micronaututils.QBiCDataSource
import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace
import org.apache.groovy.dateutil.extensions.DateUtilExtensions

import javax.inject.Inject
import javax.inject.Singleton
import java.sql.Clob
import java.text.DateFormat
import java.text.SimpleDateFormat

@Log4j2
@Singleton
class MariaDBStorage implements WeblogStorage, AutoCloseable{

    private static final DateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private static final DateFormat utcDateFormat = new SimpleDateFormat(Constants.ISO_8601_DATETIME_FORMAT)

    private QBiCDataSource dataSource

    private Sql sql

    @Inject MariaDBStorage(QBiCDataSource dataSource) {
        this.dataSource = dataSource
    }

    List<Trace> findTracesForRunWithId(String id) {
        sql = new Sql(dataSource.source)
        try {
            def result = tryToFetchTracesForRun(id)
            sql.close()
            return result
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not fetch trace information for run with id $id.", e.fillInStackTrace())
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
        def result = sql.rows("""SELECT * FROM traces WHERE runId=$key;""")
        List<Trace> traces = result.collect{ convertRowResultToTrace(it) }
        return traces
    }

    private static Trace convertRowResultToTrace(GroovyRowResult row) {
        return new Trace(["task_id": row.get('TASKID'),
            "start": row.get('STARTTIME'),
            "submit": row.get('SUBMISSIONTIME'),
            "name": row.get('NAME'),
            "status": row.get('STATUS'),
            "exit": row.get('EXIT'),
            "attempt": row.get('ATTEMPT'),
            "memory": row.get('MEMORY'),
            "duration": row.get('DURATION'),
            "cpus": row.get('CPUS'),
            "queue": row.get('QUEUE')])
    }

    List<RunInfo> findRunWithRunId(String runId) {
        sql = new Sql(dataSource.source)
        try {
            def result = tryToFindWeblogEntryWithRunId(runId)
            def weblogMessages = result.collect { convertRowResultToRunInfo(it) }
            sql.close()
            return weblogMessages
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not query weblog message with run id $runId! Reason: $e", e.fillInStackTrace())
        }
    }

    private List<GroovyRowResult> tryToFindWeblogEntryWithRunId(String runId) {
        final def statement = "SELECT * FROM runs WHERE runId='${runId}';"
        return sql.rows(statement)
    }

    private static RunInfo convertRowResultToRunInfo(GroovyRowResult rowResult) {
        RunInfo info = new RunInfo()
        info.event = rowResult.get("LASTEVENT" )
        info.id = rowResult.get("RUNID")
        info.status = rowResult.get("LASTEVENT" )
        info.name = rowResult.get("NAME")
        info.time = utcDateFormat.parse(toUTCTime(rowResult.get("LASTRECORD") as String))
        return info
    }

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException{
       this.sql = new Sql(dataSource.source)
        try {
            tryToStoreWeblogMessage(message)
            sql.close()
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not store weblog message: $message! Reason: $e", e.fillInStackTrace())
        }
    }

    private void tryToStoreWeblogMessage(WeblogMessage message) {
        def primaryKeyRun = storeRunInfo(message.runInfo)
        insertTraceInfo(message.trace, primaryKeyRun)
        insertMetadataInfo(message.metadata, primaryKeyRun)
    }

    private void insertMetadataInfo(MetaData metaData, Integer primaryKeyRun) {
        if( metaData == new MetaData() ) {
            return
        }
        sql.execute("""insert into metadata (runId,
            startTime, parameters, workDir, container, user, manifest,
            revision, duration, success, resume, nextflowVersion, exitStatus, errorMessage) 
            values (
                $primaryKeyRun,
                ${ utcDateFormat.parse(metaData.workflow.'start' as String) },
                ${JsonOutput.toJson(metaData.'parameters')},
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
        if( isRunInfoStored(runInfo) ) {
            primaryKey = updateWeblogRunInfo(runInfo)
            log.info "Updated run info for run with id ${runInfo.id}"
        } else {
            primaryKey = insertWeblogRunInfo(runInfo)
        }
        return primaryKey
    }

    private boolean isRunInfoStored(RunInfo runInfo) {
        return tryToFindWeblogEntryWithRunId(runInfo.id)
    }

    private Integer updateWeblogRunInfo(RunInfo runInfo){
        sql.execute(""" update runs set lastEvent = ${runInfo.event.toString()}, \
                lastRecord = ${runInfo.time} where runId = ${runInfo.id} and name = ${runInfo.name};""")
        def result = tryToFindWeblogEntryWithRunId(runInfo.id)
        return result[0].get('id') as Integer
    }

    private Integer insertWeblogRunInfo(RunInfo runInfo) {
        sql.execute("""insert into runs (runId, name, lastEvent, lastRecord) values \
            ($runInfo.id,
            $runInfo.name,
            ${runInfo.event.toString()},
            ${ runInfo.time });""")
        def result = tryToFindWeblogEntryWithRunId(runInfo.id)
        if( !result ) {
            throw new WeblogStorageException("Insertion went wrong!")
        }
        return result[0].get('id') as Integer

    }

    private void insertTraceInfo(Trace trace, Integer primaryKeyRun) {
        if( trace == new Trace() )
            return
        sql.execute("""insert into traces (taskId, runId, startTime, submissionTime, name, `status`, `exit`, attempt, memory, cpus, queue, duration) values \
            (${trace.'task_id'},
            $primaryKeyRun,
            ${trace.'start'},
            ${trace.'submit'},
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
        sql = new Sql(dataSource.source)
        try {
            def result = tryToFetchMetadataForRun(id)
            sql.close()
            return result
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not retrieve metadata information for run with id: $id. Reason: $e", e)
        }
    }

    private List<MetaData> tryToFetchMetadataForRun(String runId) {
        def resultRows = tryToFindWeblogEntryWithRunId(runId)
        if( resultRows.size() > 1 ) {
            throw new WeblogStorageException("More than one run found for id $runId!. Expected unique result.")
        }
        def primaryKeyRun = resultRows.get(0)["ID"] as Integer
        return findMetadataForRunWithForeignKey(primaryKeyRun)
    }

    private List<MetaData> findMetadataForRunWithForeignKey(Integer key) {
        def result = sql.rows("SELECT * FROM metadata WHERE runId=$key;")
        List<MetaData> metadata = result.collect{ convertRowResultToMetadata(it) }
        return metadata
    }

    private static MetaData convertRowResultToMetadata(GroovyRowResult rowResult) {
        def slurper = new JsonSlurper()
        def workflow = [
                'start': toUTCTime(rowResult.get('STARTTIME') as String),
                'workDir': rowResult.get('WORKDIR'),
                'container': rowResult.get('CONTAINER'),
                'userName': rowResult.get('USER'),
                'manifest': slurper.parseText(parseClob(rowResult.get('MANIFEST') ?: '')),
                'revision': rowResult.get('REVISION'),
                'duration': rowResult.get('DURATION'),
                'success': rowResult.get('SUCCESS'),
                'resume': rowResult.get('RESUME'),
                'model': ['version': rowResult.get('NEXTFLOWVERSION')],
                'exitStatus': rowResult.get('EXITSTATUS'),
                'errorMessage': rowResult.get('ERRORMESSAGE')
        ]

        return new MetaData([
                'parameters': slurper.parseText(parseClob(rowResult.get('PARAMETERS') ?: '')),
                'workflow': workflow
        ])
    }

    @Override
    List<RunInfo> findAllRunInfo() {
        this.sql = new Sql(dataSource.source)
        try {
            def runInfoList = tryToFindAllRunInfo()
            sql.close()
            return runInfoList
        } catch (Exception e) {
            sql.close()
            throw new WeblogStorageException("Could not request all run info Reason: $e", e.fillInStackTrace())
        }
    }

    private List<RunInfo> tryToFindAllRunInfo() {
        def result = sql.rows("SELECT * FROM runs ORDER BY lastRecord DESC;")
        List<RunInfo> runInfoList = result.collect{ convertRowResultToRunInfo(it) }
        return runInfoList
    }

    private static String parseClob(Object clob) {
        if (! clob) {
            return "{}"
        }
        if (! (clob instanceof Clob)){
            return clob.toString()
        }
        Reader reader = (clob as Clob).getCharacterStream()
        return reader.getText()
    }

    private static String toUTCTime(String timestamp) {
        def parsedDate = databaseDateFormat.parse(timestamp)
        return DateUtilExtensions.format(parsedDate, Constants.ISO_8601_DATETIME_FORMAT)
    }

    @Override
    void close() throws Exception {
        sql.close()
        dataSource.connection.close()
    }
}
