package life.qbic.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import life.qbic.interaction.WeblogInteractor

import javax.inject.Inject

@Controller("/messages")
class WeblogController {

    private final WeblogInteractor interactor

    @Inject WeblogController(WeblogInteractor interactor) {
        this.interactor = interactor
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post
    HttpResponse storeWeblogMessage() {
        println "Kriege mer na."
        return HttpResponse.ok()
    }

}
