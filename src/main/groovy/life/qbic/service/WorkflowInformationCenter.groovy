package life.qbic.service

import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkflowInformationCenter implements WorkflowService {

    private final WeblogStorage storage

    @Inject WorkflowInformationCenter(WeblogStorage storage) {
        this.storage = storage
    }

    @Override
    void storeWeblogMessage(WeblogMessage message) {
        storage.storeWeblogMessage(message)
    }

    @Override
    List<RunInfo> getWorkflowRunInfoForId(String runId) {
        storage.findRunWithRunId(runId)
    }

    @Override
    List<Trace> getTracesForWorkflowWithId(String runId) {
        return null
    }

    @Override
    List<MetaData> getMetadataOfWorkflow(String runId) {
        return null
    }
}
