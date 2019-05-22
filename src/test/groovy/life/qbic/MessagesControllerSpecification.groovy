package life.qbic

import groovy.json.JsonSlurper
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.RunInfo
import life.qbic.service.WorkflowService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject


@MicronautTest(environments=['test'])
class MessagesControllerSpecification extends Specification {

    @Inject
    ApplicationContext context

    @Shared
    String messageWithTrace

    @Shared
    String messageWithMetadata

    def setupSpec() {
        messageWithTrace = new File("src/test/resources/WeblogPayloadWithTrace.json").text
        messageWithMetadata = new File("src/test/resources/WeblogPayloadWithMetaData.json").text
    }

    @Inject
    @Client("/")
    RxHttpClient client

    @Unroll
    void "store a weblog payload with metadata and return resource location"() {
        given:
        WeblogMessage message = WeblogMessage.createFromJson(messageWithMetadata)

        when:
        HttpRequest request = HttpRequest.POST('/messages', messageWithMetadata)
        HttpResponse result = client.toBlocking().exchange(request)

        then:
        assert context.containsBean(WorkflowService)
        assert result.status() == HttpStatus.CREATED
        assert result.getHeaders().get("Location") == "/messages/${message.runInfo.id}"
    }

    void "after a successful first weblog submission, the resource should show basic workflow information "() {
        given:
        WeblogMessage message = WeblogMessage.createFromJson(messageWithMetadata)
        JsonSlurper slurper = new JsonSlurper()

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

    URI createWeblogResource(String message) {
        HttpRequest request = HttpRequest.POST('/messages', message)
        HttpResponse result = client.toBlocking().exchange(request)
        new URI(result.getHeaders().get("Location"))
    }


}
