package life.qbic.database

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import life.qbic.nextflow.WeblogMessage

import javax.inject.Singleton
import java.sql.Connection
import java.sql.DriverManager

@Singleton
@Requires(property = "app.db.host")
@Requires(property = "app.db.port")
@Requires(property = "app.db.name")
@Requires(property = "app.db.pw")
@Requires(property = "app.db.user")
@Requires(property = "app.db.driver.class")
@Requires(property = 'app.db.driver.prefix')
class MariaDBStorageImplementation implements WeblogStorage{

    private Connection connection

    private String databaseUrl

    private DatabaseProperties databaseProperties

    MariaDBStorageImplementation(@Property(name = 'app.db.host') String databaseHost,
                                 @Property(name = 'app.db.port') String databasePort,
                                 @Property(name = 'app.db.name') String databaseName,
                                 @Property(name = 'app.db.user') String databaseUser,
                                 @Property(name = 'app.db.pw') String userPassword,
                                 @Property(name = 'app.db.driver.class') String driverClass,
                                 @Property(name = 'app.db.driver.prefix') String driverPrefix) {
        databaseProperties = new DatabaseProperties().tap {
            it.databaseName = databaseName
            it.databasePort = databasePort
            it.databaseHost = databaseHost
            it.driverPrefix = driverPrefix
            it.driverClass = driverClass
        }

        buildDatabaseUrlFromProperties()
        createDatabaseConnectionWithCredentials(databaseUser, userPassword)
    }

    private void createDatabaseConnectionWithCredentials(String user, String password) throws Exception {
        Class.forName(databaseProperties.driverClass)
        connection = DriverManager.getConnection(databaseUrl, user, password)
    }

    private void buildDatabaseUrlFromProperties(){
        databaseUrl = databaseProperties.getDatabaseConnectionUrl()
    }

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException{
        try {
            tryToStoreWeblogMessage(message)
        } catch (Exception e) {
            throw new WeblogStorageException("Could not store weblog message: $message", e)
        }
    }

    private void tryToStoreWeblogMessage(WeblogMessage message) {

    }

    private static class DatabaseProperties {

        private String databasePort, databaseHost, databaseName, driverPrefix, driverClass

        private String getUrl() {
            "${driverPrefix}://${databaseHost}:${databasePort}/${databaseName}"
        }

        private String getUrlWithPort() {
            "${driverPrefix}://${databaseHost}/${databaseName}"
        }

        String getDatabaseConnectionUrl() {
            final String url
            if (databasePort) {
                url = getUrlWithPort()
            } else {
                url = getUrl()
            }
            return url
        }

    }
}
