package life.qbic

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.reactivex.Flowable
import io.reactivex.Maybe
import life.qbic.service.WorkflowService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@MicronautTest(environments = ['test'])
class MessagesControllerSpecification extends Specification {

    @Inject
    WorkflowService workflowService

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
    void "test"() {
        when:
        HttpRequest request = HttpRequest.POST('/messages', messageWithMetadata)
        HttpResponse result = client.toBlocking().exchange(request)

        then:
        //1 * workflowService.storeWeblogMessage(messageWithMetadata)
        assert result.status() == HttpStatus.ACCEPTED
    }


}
