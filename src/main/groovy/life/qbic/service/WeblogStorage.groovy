package life.qbic.service

import life.qbic.database.WeblogStorageException
import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.Trace

import javax.inject.Singleton

@Singleton
interface WeblogStorage {

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException

    List<WeblogMessage> findRunWithRunId(String runId)

    List<Trace> findTracesForRunWithId(String id)

    List<MetaData> findMetadataForRunWithId(String id)

}
