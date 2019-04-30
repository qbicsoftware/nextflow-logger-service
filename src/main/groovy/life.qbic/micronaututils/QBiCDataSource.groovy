package life.qbic.micronaututils

import javax.inject.Inject
import javax.inject.Singleton
import java.sql.Connection

@Singleton
class QBiCDataSource implements DataSource {

    javax.sql.DataSource source

    @Inject QBiCDataSource (javax.sql.DataSource source) {
        this.source = source
    }

    @Override
    Connection getConnection() {
        return this.source.connection
    }
}
