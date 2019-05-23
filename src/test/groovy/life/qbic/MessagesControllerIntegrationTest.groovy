package life.qbic

import groovy.json.JsonSlurper
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace
import life.qbic.service.WorkflowService
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject


@MicronautTest
class MessagesControllerIntegrationTest extends Specification {

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

    void "store a weblog payload with metadata and return resource location"() {
        given:
        WeblogMessage message = WeblogMessage.createFromJson(messageWithMetadata)

        when:
        HttpRequest request = HttpRequest.POST('/workflows', messageWithMetadata)
        HttpResponse result = client.toBlocking().exchange(request)

        then:
        assert context.containsBean(WorkflowService)
        assert result.status() == HttpStatus.CREATED
        assert result.getHeaders().get("Location") == "/workflows/info/${message.runInfo.id}"
    }

    void "after a successful first weblog submission, the resource should show basic workflow information "() {
        given:
        WeblogMessage message = WeblogMessage.createFromJson(messageWithMetadata)

        when:
        URI resourceLocation = createWeblogResource(messageWithMetadata)
        HttpRequest request = HttpRequest.GET(resourceLocation)
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
        HttpRequest request = HttpRequest.GET(nonExistingResourceLocation)
        client.toBlocking().exchange(request)

        then:
        HttpClientResponseException ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
    }

    void "/workflows/traces/{runId} access trace information for a workflow successfully"() {
        given:
        WeblogMessage message = WeblogMessage.createFromJson(messageWithTrace)

        when:
        createWeblogResource(messageWithTrace)
        URI traceResourceLocation = new URI("/workflows/traces/${message.runInfo.id}")
        HttpRequest request = HttpRequest.GET(traceResourceLocation)
        HttpResponse result = client.toBlocking().exchange(request, String)

        then:
        assert result.body()
        List<Map> traces = (slurper.parseText(result.body()) as List)
        assert traces.size() == 1
        Trace trace = new Trace(traces.get(0).properties as Map)
        compareTraces(trace, message.trace)
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
