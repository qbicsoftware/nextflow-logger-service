package life.qbic.weblog

import life.qbic.weblog.nextflow.MetaData
import life.qbic.weblog.nextflow.RunInfo
import life.qbic.weblog.nextflow.Trace

class NextflowWeblogMessage {

    private RunInfo runInfo

    private Trace trace

    private MetaData metadata
    
    private NextflowWeblogMessage(){}

    /**
    static NextflowWeblogMessage withRunAndTraceInfo(RunInfo runInfo,
                                                     Trace trace){
       return new NextflowWeblogMessage().tap {
           this.runInfo = runInfo
           this.trace = trace
           this.metadata = new MetaData()
       }
    }

    static NextflowWeblogMessage withTraceAndMetadataInfo(RunInfo runInfo,
                                                          Trace trace,
                                                          MetaData metadata){
        return new NextflowWeblogMessage().tap {
            this.runInfo = runInfo
            this.trace = trace
            this.metadata = metadata
        }
    }*/

    static NextflowWeblogMessage withRunInfo(RunInfo info){
        return new NextflowWeblogMessage().tap {
            runInfo = info
            trace = new Trace()
            metadata = new MetaData()
        }
    }

}
