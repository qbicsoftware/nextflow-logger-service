package life.qbic.flowstore.domain

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.json.JsonSlurper
import life.qbic.datamodel.workflows.MetaData
import life.qbic.datamodel.workflows.RunInfo
import life.qbic.datamodel.workflows.Trace

class Workflow {

    static final String TRACE_FIELDNAME = 'trace'

    static final String METADATA_FIELDNAME = 'metadata'

    @JsonProperty("runinfo")
    private RunInfo runInfo

    @JsonProperty("trace")
    private Trace trace

    @JsonProperty("metadata")
    private MetaData metadata
    
    private Workflow(){}

    static Workflow createFromJson(String json) {
        final def messageProperties = new JsonSlurper(checkDates: true).parseText(json) as Map
        return new Workflow().tap {
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

    static Workflow withRunAndTraceInfo(RunInfo runInfo,
                                        Trace trace){
       return new Workflow().tap {
           it.runInfo = runInfo
           it.trace = trace
           metadata = new MetaData()
       }
    }

    static Workflow withRunAndMetadataInfo(RunInfo runInfo,
                                           MetaData metadata){
        return new Workflow().tap {
            it.runInfo = runInfo
            it.trace = new Trace()
            it.metadata = metadata
        }
    }

    static Workflow withRunInfo(RunInfo runInfo){
        return new Workflow().tap {
            it.runInfo = runInfo
            trace = new Trace()
            metadata = new MetaData()
        }
    }

}
