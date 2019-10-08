package life.qbic.flowstore.endpoints

import groovy.util.logging.Log4j2
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import life.qbic.flowstore.domain.Contact
import life.qbic.flowstore.domain.Workflow
import life.qbic.flowstore.domain.MetaData
import life.qbic.flowstore.domain.RunInfo
import life.qbic.flowstore.domain.Trace
import life.qbic.flowstore.domain.WorkflowService
import life.qbic.micronaututils.auth.Authentication

import javax.inject.Inject

@Log4j2
@Requires(beans = Authentication.class)
@Controller("/workflows")
class WorkflowsController {

    WorkflowService informationCenter

    private Contact contact

    @Inject
    WorkflowsController(WorkflowService informationCenter, Contact contact) {
        this.informationCenter = informationCenter
        this.contact = contact
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri="/")
    HttpResponse storeWeblogMessage(@Body String message) {
        Workflow weblogMessage
        try {
            weblogMessage = Workflow.createFromJson(message)
            log.info "Incoming weblog message with for run ID: ${weblogMessage.runInfo.id}"
            informationCenter.storeWeblogMessage(weblogMessage)
        } catch ( Exception e ) {
            log.error(e)
            log.debug(e.printStackTrace())
            return HttpResponse.serverError("Unexpected error, resource could not be created.")
        }
        return HttpResponse.created(new URI("/workflows/info/${weblogMessage.runInfo.id}"))
    }

    @Secured("READER")
    @Get("/info/{runId}")
    HttpResponse getBasicWorkflowInformation(String runId) {
        log.info "Resource request for runId: $runId."
        List<RunInfo> runInfoList
        try {
            runInfoList = informationCenter.getWorkflowRunInfoForId(runId)
        } catch ( Exception e ) {
            log.error(e)
            return HttpResponse.serverError(serverErrorResponse())
        }
        runInfoList ? HttpResponse.ok(runInfoList): HttpResponse.notFound()
    }

    @Secured("READER")
    @Get("/traces/{runId}")
    HttpResponse<List<Trace>> getTracesForWorkflow(String runId) {
        log.info "Traces request for runId: $runId."
        List<Trace> traces
        try {
            traces = informationCenter.getTracesForWorkflowWithId(runId)
        } catch( Exception e ) {
            log.error(e)
            return HttpResponse.serverError()
        }
        traces ? HttpResponse.ok(traces) : HttpResponse.notFound(traces)
    }

    @Secured("READER")
    @Get("/metadata/{runId}")
    HttpResponse<List<MetaData>> getMetaDataForWorkflow(String runId) {
        log.info "Metadata request for runId: $runId."
        List<MetaData> metaData
        try {
            metaData = informationCenter.getMetadataOfWorkflow(runId)
        } catch( Exception e ) {
            log.error(e)
            return HttpResponse.serverError()
        }
        metaData ? HttpResponse.ok(metaData) : HttpResponse.notFound(metaData)

    }

    @Secured("READER")
    @Get("/")
    HttpResponse<List<RunInfo>> getAllRunInfoForWorkflows() {
        log.info "Run info request."
        List<RunInfo> runInfoList
        try {
            runInfoList = informationCenter.getAllWorkflowRunInfo()
        } catch ( Exception e ) {
            log.error(e)
            return HttpResponse.serverError()
        }
        runInfoList ? HttpResponse.ok(runInfoList) : HttpResponse.notFound(runInfoList)
    }

    static String serverErrorResponse() {
        "Unexpected error, resource could not be accessed."
    }


}
