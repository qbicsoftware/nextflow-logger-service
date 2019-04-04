package life.qbic.database

import groovy.sql.Sql
import life.qbic.nextflow.WeblogMessage
import life.qbic.nextflow.weblog.Trace

import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource
import java.sql.Connection

@Singleton
class MariaDBStorageImplementation implements WeblogStorage{

    private DataSource dataSource

    @Inject MariaDBStorageImplementation(DataSource dataSource) {
        this.dataSource = dataSource
    }

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException{
        try {
            tryToStoreWeblogMessage(message, dataSource.connection)
        } catch (Exception e) {
            throw new WeblogStorageException("Could not store weblog message: $message", e)
        }
    }

    private void tryToStoreWeblogMessage(WeblogMessage message, Connection connection) {
        final def sql = new Sql(connection)
        insertRunInfo(message, sql)
        insertTraceInfo(message.trace, sql)
        sql.close()
    }

    private void insertRunInfo(WeblogMessage, Sql sql) {
        //TODO Implement run info insertion
    }

    private void insertTraceInfo(Trace trace, Sql sql) {
        //TODO Implement trace info insertion
    }
}
