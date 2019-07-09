package life.qbic.service

import life.qbic.model.WeblogMessage
import life.qbic.model.weblog.MetaData
import life.qbic.model.weblog.RunInfo
import life.qbic.model.weblog.Trace
import javax.inject.Singleton

@Singleton
interface WorkflowService {

    void storeWeblogMessage(WeblogMessage message)

    List<RunInfo> getWorkflowRunInfoForId(String runId)

    List<Trace> getTracesForWorkflowWithId(String runId)

    List<MetaData> getMetadataOfWorkflow(String runId)

    List<RunInfo> getAllWorkflowRunInfo()
}