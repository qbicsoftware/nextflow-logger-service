package life.qbic

import groovy.util.logging.Log4j2
import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.info.Contact

@OpenAPIDefinition(
        info = @Info(
                title = "Flowstore",
                version = "0.1",
                description = "Storing and querying Nextflow worfklow run information.",
                license = @License(name = "MIT", url = "http://foo.bar"),
                contact = @Contact(url = "http://qbic.life", name = "Sven Fillinger", email = "sven.fillinger@qbic.uni-tuebingen.de")
        )
)
@CompileStatic
@Log4j2
class Application {
    static void main(String[] args) {
        def envVars = System.getenv()
        log.info(envVars['WF_DB_HOST'])
        log.info(envVars['WF_DB_NAME'])
        log.info(envVars['WF_DB_PWD'])
        log.info(envVars['WF_DB_USER'])
        Micronaut.run(Application)
    }
}