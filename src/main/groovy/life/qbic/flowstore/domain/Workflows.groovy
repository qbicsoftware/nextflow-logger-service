package life.qbic.flowstore.domain


import javax.inject.Singleton

@Singleton
interface Workflows {

    void storeWeblogMessage(Workflow message) throws WeblogStorageException

    List<RunInfo> findRunWithRunId(String runId)

    List<Trace> findTracesForRunWithId(String id)

    List<MetaData> findMetadataForRunWithId(String id)

    List<RunInfo> findAllRunInfo()

}
