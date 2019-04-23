package life.qbic.database

import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.*
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class MariaDBStorageIntegrationTest extends Specification{

    @Inject
    WeblogStorage storage

    def "test"() {
        given:
        def x = 1

        when:
        x

        then:
        assert x == 1
    }

}
