package life.qbic.controller

import groovy.util.logging.Log4j2
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import life.qbic.Contact
import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace
import life.qbic.service.WorkflowService

import javax.inject.Inject

@Log4j2
@Controller("/workflows")
class MessagesController {

    WorkflowService informationCenter

    private Contact contact

    @Inject MessagesController(WorkflowService informationCenter, Contact contact) {
        this.informationCenter = informationCenter
        this.contact = contact
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri="/")
    HttpResponse storeWeblogMessage(@Body String message) {
        WeblogMessage weblogMessage
        try {
            weblogMessage = WeblogMessage.createFromJson(message)
            log.info("Incoming weblog message with for run id: ${weblogMessage.runInfo.id}")
            informationCenter.storeWeblogMessage(weblogMessage)
        } catch ( Exception e ) {
            log.error(e)
            log.debug(e.printStackTrace())
            return HttpResponse.serverError("Unexpected error, resource could not be created.")
        }
        return HttpResponse.created(new URI("/workflows/info/${weblogMessage.runInfo.id}"))
    }

    @Get("/info/{runId}")
    HttpResponse getBasicWorkflowInformation(String runId) {
        log.info("Resource request for runId: $runId.")

        List<RunInfo> runInfoList
        try {
            runInfoList = informationCenter.getWorkflowRunInfoForId(runId)
        } catch ( Exception e ) {
            log.error(e)
            return HttpResponse.serverError(serverErrorResponse())
        }
        runInfoList ? HttpResponse.ok(runInfoList): HttpResponse.notFound()
    }

    @Get("/traces/{runId}")
    HttpResponse<List<Trace>> getTracesForWorkflow(String runId) {
        log.info("Traces request for runId: $runId.")
        List<Trace> traces
        try {
            traces = informationCenter.getTracesForWorkflowWithId(runId)
        } catch( Exception e ) {
            log.error(e)
            return HttpResponse.serverError()
        }
        traces ? HttpResponse.ok(traces) : HttpResponse.notFound(traces)
    }

    @Get("/metainfo/{runId}")
    HttpResponse<List<MetaData>> getMetaDataForWorkflow(String runId) {
        log.info("Metadata request for runId: $runId.")
        List<MetaData> metaData
        try {
            metaData = informationCenter.getMetadataOfWorkflow(runId)
        } catch( Exception e ) {
            log.error(e)
            return HttpResponse.serverError()
        }
        metaData ? HttpResponse.ok(metaData) : HttpResponse.notFound(metaData)

    }

    static String serverErrorResponse() {
        "Unexpected error, resource could not be accessed."
    }


}
