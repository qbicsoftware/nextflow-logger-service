package life.qbic.database

import life.qbic.nextflow.WeblogMessage
import life.qbic.nextflow.weblog.Trace

import javax.inject.Singleton

@Singleton
interface WeblogStorage {

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException

    List<WeblogMessage> findWeblogEntryWithRunId(String runId)

    List<Trace> findTracesForRunWithId(String id)

}
