package life.qbic.handler

import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace

class WorkflowInformationCenter implements Handler {
    @Override
    def storeWeblogMessage(WeblogMessage message) {
        return null
    }

    @Override
    RunInfo getWorkflowRunInfoForId(String runId) {
        return null
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
