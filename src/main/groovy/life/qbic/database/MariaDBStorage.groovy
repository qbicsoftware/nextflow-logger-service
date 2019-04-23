package life.qbic.database

import groovy.sql.Sql
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import life.qbic.nextflow.WeblogMessage
import life.qbic.nextflow.weblog.MetaData
import life.qbic.nextflow.weblog.RunInfo
import life.qbic.nextflow.weblog.Trace

import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource

@Singleton
@Requires(property='database.name', defaultValue="workflows")
@Requires(property='database.tables.runs', defaultValue="runs")
@Requires(property='database.tables.traces', defaultValue="traces" )
class MariaDBStorage implements WeblogStorage{

    private DataSource dataSource

    @Value('${database.name}')
    private static String WF_DATABASE_NAME

    @Value('${database.tables.runs}')
    private static String WF_RUNS_TABLE

    @Value('${database.tables.traces}')
    private static String WF_TRACES_TABLE

    @Inject MariaDBStorageImplementation(DataSource dataSource) {
        this.dataSource = dataSource
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

    private void tryToStoreWeblogMessage(WeblogMessage message, Sql sql) {
        insertRunInfo(message.runInfo, sql)
        insertTraceInfo(message.trace, sql)
        insertMetadataInfo(message.metadata, sql)
    }

    private void insertMetadataInfo(MetaData metaData, Sql sql) {
        //TODO Implement metadata insertion
    }


    private void insertRunInfo(RunInfo runInfo, Sql sql) {
        sql.execute("insert into ${WF_RUNS_TABLE} (runId) values ($runInfo.id)")
    }

    private void insertTraceInfo(Trace trace, Sql sql) {
        //TODO Implement trace info insertion
    }
}
