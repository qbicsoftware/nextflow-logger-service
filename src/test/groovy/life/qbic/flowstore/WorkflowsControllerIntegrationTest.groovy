package life.qbic.flowstore

import groovy.json.JsonSlurper
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import life.qbic.datamodel.workflows.RunInfo
import life.qbic.flowstore.domain.Workflow
import life.qbic.datamodel.workflows.Trace
import life.qbic.flowstore.domain.WorkflowService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@Stepwise
@MicronautTest
class WorkflowsControllerIntegrationTest extends Specification {

    @Inject
    ApplicationContext context

    @Shared
    String messageWithTrace

    @Shared
    JsonSlurper slurper

    @Shared
    String messageWithMetadata

    def setupSpec() {
        messageWithTrace = new File("src/test/resources/WeblogPayloadWithTrace.json").text
        messageWithMetadata = new File("src/test/resources/WeblogPayloadWithMetaData.json").text
        slurper = new JsonSlurper()
    }

    @Inject
    @Client("/")
    RxHttpClient client



    void "after a successful first weblog submission, the resource should show basic workflow information "() {
        given:
        Workflow message = Workflow.createFromJson(messageWithMetadata)

        when:
        URI resourceLocation = createWeblogResource(messageWithMetadata)
        HttpRequest request = HttpRequest.GET(resourceLocation).basicAuth("servicereader", "123!")
        HttpResponse result = client.toBlocking().exchange(request, String)

        then:
        assert result.status() == HttpStatus.OK
        assert result.getBody()
        List<Map> list = (slurper.parseText(result.body()) as List)
        RunInfo runInfo = new RunInfo(list.get(0) as Map)
        assert runInfo.id == message.runInfo.id
        assert runInfo.name == message.runInfo.name
    }

    void "/workflows/info/{runId} with wrong id responds 404"() {
        when:
        URI nonExistingResourceLocation = new URI("/workflows/info/1234")
        HttpRequest request = HttpRequest.GET(nonExistingResourceLocation).basicAuth("servicereader", "123!")
        client.toBlocking().exchange(request)

        then:
        HttpClientResponseException ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
    }

    void "/workflows/metadata/{runId} access metadata information for a workflow successfully"() {
        given:
        Workflow message = Workflow.createFromJson(messageWithMetadata)

        when:
        createWeblogResource(messageWithMetadata)
        URI metadataResourceLocation = new URI("/workflows/metadata/${message.runInfo.id}")
        HttpRequest request = HttpRequest.GET(metadataResourceLocation).basicAuth("servicereader", "123!")
        HttpResponse result = client.toBlocking().exchange(request, String)

        then:
        assert result.body()
        List<Map> metadataList = (slurper.parseText(result.body()) as List)
        assert metadataList.size() >= 1
        assert metadataList[0]['metadata']['parameters'].container == message.metadata['parameters'].container
    }

    void "/workflows/traces/{runId} access trace information for a workflow successfully"() {
        given:
        Workflow message = Workflow.createFromJson(messageWithTrace)
        URI traceResourceLocation = new URI("/workflows/traces/${message.runInfo.id}")

        when:
        createWeblogResource(messageWithTrace)
        HttpRequest request = HttpRequest.GET(traceResourceLocation).basicAuth("servicereader", "123!")
        HttpResponse result = client.toBlocking().exchange(request, String)

        then:
        assert result.body()
        List<Map> traces = (slurper.parseText(result.body()) as List)
        assert traces.size() >= 1
        Trace trace = new Trace(traces.get(0).properties as Map)
        compareTraces(trace, message.trace)
    }

    void "/workflows GET request lists all available workflows run info"() {
        given:
        URI allWorkflowsRunInfoRessourceLocation = new URI("/workflows")

        when:
        createWeblogResource(messageWithMetadata)
        HttpRequest request = HttpRequest.GET(allWorkflowsRunInfoRessourceLocation).basicAuth("servicereader", "123!")
        HttpResponse result = client.toBlocking().exchange(request, String)

        then:
        assert result.body()
        List<Map> runInfoList = (slurper.parseText(result.body()) as List)
        assert runInfoList.size() >= 1
    }

    void "store a weblog payload with metadata and return resource location"() {
        given:
        Workflow message = Workflow.createFromJson(messageWithMetadata)

        when:
        HttpRequest request = HttpRequest.POST('/workflows', messageWithMetadata)
        HttpResponse result = client.toBlocking().exchange(request)

        then:
        assert context.containsBean(WorkflowService)
        assert result.status() == HttpStatus.CREATED
        assert result.getHeaders().get("Location") == "/workflows/info/${message.runInfo.id}"
    }

    void "/workflows GET without authentication should throw Exception"() {
        given:
        URI allWorkflowsRunInfoRessourceLocation = new URI("/workflows")

        when:
        createWeblogResource(messageWithMetadata)
        HttpRequest request = HttpRequest.GET(allWorkflowsRunInfoRessourceLocation)
        HttpResponse result = client.toBlocking().exchange(request, String)

        then:
        thrown HttpClientResponseException
    }



    private URI createWeblogResource(String message) {
        HttpRequest request = HttpRequest.POST('/workflows', message)
        HttpResponse result = client.toBlocking().exchange(request)
        new URI(result.getHeaders().get("Location"))
    }

    private static void compareTraces (Trace t1, Trace t2) {
        assert t1.'task_id' == t2.'task_id'
        assert t1.'status' == t2.'status'
        assert t1.'start' == t2.'start'
        assert t1.'submit' == t2.'submit'
        assert t1.'name' == t2.'name'
        assert t1.'exit' == t2.'exit'
        assert t1.'attempt' == t2.'attempt'
        assert t1.'memory' == t2.'memory'
        assert t1.'cpus' == t2.'cpus'
        assert t1.'queue' == t2.'queue'
        assert t1.'duration' == t2.'duration'
    }


}
