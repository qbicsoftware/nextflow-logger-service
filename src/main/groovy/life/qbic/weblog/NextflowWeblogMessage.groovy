package life.qbic.weblog

import life.qbic.weblog.nextflow.MetaData
import life.qbic.weblog.nextflow.RunInfo
import life.qbic.weblog.nextflow.Trace

class NextflowWeblogMessage {

    private RunInfo runInfo

    private Trace trace

    private MetaData metadata
    
    private NextflowWeblogMessage(){}


    static NextflowWeblogMessage withRunAndTraceInfo(RunInfo runInfo,
                                                     Trace trace){
       return new NextflowWeblogMessage().tap {
           it.runInfo = runInfo
           it.trace = trace
           metadata = new MetaData()
       }
    }

    static NextflowWeblogMessage withTraceAndMetadataInfo(RunInfo runInfo,
                                                          Trace trace,
                                                          MetaData metadata){
        return new NextflowWeblogMessage().tap {
            it.runInfo = runInfo
            it.trace = trace
            it.metadata = metadata
        }
    }

    static NextflowWeblogMessage withRunInfo(RunInfo runInfo){
        return new NextflowWeblogMessage().tap {
            it.runInfo = runInfo
            trace = new Trace()
            metadata = new MetaData()
        }
    }

}
