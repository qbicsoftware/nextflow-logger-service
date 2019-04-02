package life.qbic.nextflow

import groovy.transform.CompileStatic
import life.qbic.nextflow.weblog.MetaData
import life.qbic.nextflow.weblog.RunInfo
import life.qbic.nextflow.weblog.Trace

@CompileStatic
class WeblogMessage {

    private RunInfo runInfo

    private Trace trace

    private MetaData metadata
    
    private WeblogMessage(){}

    static WeblogMessage withRunAndTraceInfo(RunInfo runInfo,
                                             Trace trace){
       return new WeblogMessage().tap {
           it.runInfo = runInfo
           it.trace = trace
           metadata = new MetaData()
       }
    }

    static WeblogMessage withRunAndMetadataInfo(RunInfo runInfo,
                                                MetaData metadata){
        return new WeblogMessage().tap {
            it.runInfo = runInfo
            it.trace = new Trace()
            it.metadata = metadata
        }
    }

    static WeblogMessage withRunInfo(RunInfo runInfo){
        return new WeblogMessage().tap {
            it.runInfo = runInfo
            trace = new Trace()
            metadata = new MetaData()
        }
    }

}
