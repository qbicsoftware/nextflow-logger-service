package life.qbic

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
class Application {
    static void main(String[] args) {
        Micronaut.run(Application)
    }
}