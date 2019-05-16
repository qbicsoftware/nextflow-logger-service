package life.qbic.handler

import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace

interface Handler {

    storeWeblogMessage(WeblogMessage message)

    RunInfo getWorkflowRunInfoForId(String runId)

    List<Trace> getTracesForWorkflowWithId(String runId)

    List<MetaData> getMetadataOfWorkflow(String runId)
}