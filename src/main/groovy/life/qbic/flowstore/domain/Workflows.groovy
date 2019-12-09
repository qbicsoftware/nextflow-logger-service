package life.qbic.flowstore.domain

import life.qbic.datamodel.workflows.MetaData
import life.qbic.datamodel.workflows.RunInfo
import life.qbic.datamodel.workflows.Trace

import javax.inject.Singleton

@Singleton
interface Workflows {

    void storeWeblogMessage(Workflow message) throws WeblogStorageException

    List<RunInfo> findRunWithRunId(String runId)

    List<Trace> findTracesForRunWithId(String id)

    List<MetaData> findMetadataForRunWithId(String id)

    List<RunInfo> findAllRunInfo()

}
