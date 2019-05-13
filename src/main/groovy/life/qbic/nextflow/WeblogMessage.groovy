package life.qbic.nextflow

import groovy.json.JsonSlurper
import life.qbic.nextflow.weblog.MetaData
import life.qbic.nextflow.weblog.RunInfo
import life.qbic.nextflow.weblog.Trace

class WeblogMessage {

    static final String TRACE_FIELDNAME = 'trace'

    static final String METADATA_FIELDNAME = 'metadata'

    private RunInfo runInfo

    private Trace trace

    private MetaData metadata
    
    private WeblogMessage(){}

    static WeblogMessage createFromJson(String json) {
        final def messageProperties = new JsonSlurper(checkDates: true).parseText(json) as Map
        return new WeblogMessage().tap {
            it.runInfo = new RunInfo(messageProperties)
            it.trace = createTraceInfoFromMap(messageProperties)
            it.metadata = createMetadataFromJson(messageProperties)
        }
    }

    private static MetaData createMetadataFromJson(Map map) {
        final MetaData metaData
        if (map.get(METADATA_FIELDNAME)) {
            metaData = new MetaData(map.get(METADATA_FIELDNAME) as Map)
        } else {
            metaData = new MetaData()
        }
        return metaData
    }

    private static Trace createTraceInfoFromMap(Map map) {
        final Trace trace
        if (map.get(TRACE_FIELDNAME)) {
            trace = new Trace(map.get(TRACE_FIELDNAME) as Map)
        } else {
            trace = new Trace()
        }
        return trace
    }

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
