package life.qbic.micronaututils

import java.sql.Connection

interface DataSource {

    Connection getConnection()

}