package life.qbic.flowstore.domain


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkflowInfoCenter implements WorkflowService {

    private final Workflows storage

    @Inject
    WorkflowInfoCenter(Workflows storage) {
        this.storage = storage
    }

    @Override
    void storeWeblogMessage(Workflow message) {
        storage.storeWeblogMessage(message)
    }

    @Override
    List<RunInfo> getWorkflowRunInfoForId(String runId) {
        storage.findRunWithRunId(runId)
    }

    @Override
    List<Trace> getTracesForWorkflowWithId(String runId) {
        storage.findTracesForRunWithId(runId)
    }

    @Override
    List<MetaData> getMetadataOfWorkflow(String runId) {
        storage.findMetadataForRunWithId(runId)
    }

    @Override
    List<RunInfo> getAllWorkflowRunInfo() {
        storage.findAllRunInfo()
    }
}
