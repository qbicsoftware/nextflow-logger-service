package life.qbic.controller

import groovy.util.logging.Log4j2
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import life.qbic.Contact
import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.RunInfo
import life.qbic.service.WorkflowService

import javax.inject.Inject

@Log4j2
@Controller("/messages")
class MessagesController {

    WorkflowService informationCenter

    private Contact contact

    @Inject WeblogController(WorkflowService informationCenter, Contact contact) {
        this.informationCenter = informationCenter
        this.contact = contact
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri="/")
    HttpResponse storeWeblogMessage(@Body String message) {
        WeblogMessage weblogMessage
        try {
            weblogMessage = WeblogMessage.createFromJson(message)
            log.debug("Incoming weblog message with for run id: ${weblogMessage.runInfo.id}")
            informationCenter.storeWeblogMessage(weblogMessage)
        } catch ( Exception e ) {
            log.error(e)
            return HttpResponse.serverError("Unexpected error, resource could not be created.")
        }
        return HttpResponse.created(new URI("/messages/${weblogMessage.runInfo.id}"))
    }

    @Get("/{runId}")
    HttpResponse getBasicWorkflowInformation(String runId) {
        log.debug("Resource request for runId: $runId.")

        List<RunInfo> runInfo
        try {
            runInfo = informationCenter.getWorkflowRunInfoForId(runId)
        } catch ( Exception e ) {
            log.error(e)
            return HttpResponse.serverError("Unexpected error, resource could not be accessed.")
                    .contentType(MediaType.TEXT_PLAIN)
        }

        return HttpResponse.ok(runInfo)
    }


}
