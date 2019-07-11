package life.qbic

import groovy.util.logging.Log4j2
import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.info.Contact
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator

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
class Flowstore {
    static void main(String[] args) {
        log.info "Flowstore started."
        registerShutdownHook()
        Micronaut.run(Flowstore)
    }

    static void registerShutdownHook() {
        Runtime.runtime.addShutdownHook(new Thread(new Runnable() {
            @Override
            void run() {
                log.info "Flowstore shutting down ..."
                Configurator.shutdown(LogManager.getContext() as LoggerContext)
            }
        }))
    }

}