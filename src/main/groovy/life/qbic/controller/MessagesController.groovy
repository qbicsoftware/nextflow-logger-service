package life.qbic.controller

import groovy.util.logging.Log4j2
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import life.qbic.Contact
import life.qbic.service.WorkflowService

import javax.inject.Inject

@Log4j2
@Controller("/messages")
class MessagesController {

    private WorkflowService informationCenter

    private Contact contact

    @Inject WeblogController(WorkflowService informationCenter, Contact contact) {
        this.informationCenter = informationCenter
        this.contact = contact
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri = "/")
    HttpResponse storeWeblogMessage(@Body String message) {
        try {
            log.info("Incoming message")
        } catch ( Exception e ) {
            log.error(e)
            return HttpResponse.serverError("Unexpected error, resource could not be created.")
        }
        return HttpResponse.created(new URI('/messages/8173-1234'))
    }
}
