package life.qbic.flowstore.domain


import javax.inject.Singleton

@Singleton
interface WorkflowService {

    void storeWeblogMessage(Workflow message)

    List<RunInfo> getWorkflowRunInfoForId(String runId)

    List<Trace> getTracesForWorkflowWithId(String runId)

    List<MetaData> getMetadataOfWorkflow(String runId)

    List<RunInfo> getAllWorkflowRunInfo()
}