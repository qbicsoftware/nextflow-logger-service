package life.qbic.database

import life.qbic.nextflow.WeblogMessage

import javax.inject.Singleton

@Singleton
interface WeblogStorage {

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException

    List<WeblogMessage> findWeblogEntryWithRunId(String runId)

}
