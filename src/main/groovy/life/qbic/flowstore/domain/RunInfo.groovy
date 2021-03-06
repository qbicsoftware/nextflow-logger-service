package life.qbic.flowstore.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import life.qbic.flowstore.Constants

import java.text.SimpleDateFormat

enum NextflowEventType
{
    STARTED, PROCESS_SUBMITTED, PROCESS_STARTED, PROCESS_COMPLETED, ERROR, COMPLETED, UNKNOWN
}

@EqualsAndHashCode()
class RunInfo {

    @JsonProperty("event")
    private NextflowEventType event

    @JsonProperty("runName")
    private String name

    @JsonProperty("runStatus")
    private String status

    @JsonProperty("runId")
    private String id

    @JsonProperty("utcTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.ISO_8601_DATETIME_FORMAT)
    private Date time

    RunInfo(){
        event = NextflowEventType.UNKNOWN
        name = ""
        status = ""
        id = ""
        time = new Date()
    }

    RunInfo(Map weblogRunInformation){
        event = convertEventStringToEventType(weblogRunInformation.get('event') as String)
        name = weblogRunInformation.get('runName') ?: ""
        status = weblogRunInformation.get('runStatus') ?: ""
        id = weblogRunInformation.get('runId') ?: ""
        time = convertStringToDate(weblogRunInformation.get('utcTime') as String)
    }

    private static Date convertStringToDate(String s) {
        Date date = new SimpleDateFormat(Constants.ISO_8601_DATETIME_FORMAT).parse(s)
        return date
    }

    private static NextflowEventType convertEventStringToEventType(String event){
        try {
            def eventType = event.toUpperCase() as NextflowEventType
            return eventType
        } catch (IllegalArgumentException e){
            return NextflowEventType.UNKNOWN
        }
    }

}
