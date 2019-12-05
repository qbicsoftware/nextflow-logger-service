package life.qbic.flowstore.domain

import life.qbic.datamodel.workflows.MetaData
import life.qbic.datamodel.workflows.RunInfo
import life.qbic.datamodel.workflows.Trace

import javax.inject.Singleton

@Singleton
interface WorkflowService {

    void storeWeblogMessage(Workflow message)

    List<RunInfo> getWorkflowRunInfoForId(String runId)

    List<Trace> getTracesForWorkflowWithId(String runId)

    List<MetaData> getMetadataOfWorkflow(String runId)

    List<RunInfo> getAllWorkflowRunInfo()
}