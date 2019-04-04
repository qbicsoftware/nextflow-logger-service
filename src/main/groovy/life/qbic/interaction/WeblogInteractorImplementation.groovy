package life.qbic.interaction

import life.qbic.database.WeblogStorage
import life.qbic.database.WeblogStorageException
import life.qbic.nextflow.WeblogMessage

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeblogInteractorImplementation implements WeblogInteractor{

    private final WeblogStorage storage

    @Inject WeblogInteractorImplementation(WeblogStorage storage) {
        this.storage = storage
    }

    void storeWeblogJsonPayload(String weblogPayload) throws InteractionException {
        try {
            tryStoreWeblogJsonPayload(weblogPayload)
        } catch (WeblogStorageException e1) {
            throw new InteractionException("An exception occured when interacting with the storage of payload: $weblogPayload.", e1)
        } catch (Exception e2) {
            throw new InteractionException("An unknown exception occured for weblog payload: $weblogPayload.", e2)
        }
    }

    private void tryStoreWeblogJsonPayload(String payload) {
        final WeblogMessage message = WeblogMessage.createFromJson(payload)
        storage.storeWeblogMessage(message)
    }
}
