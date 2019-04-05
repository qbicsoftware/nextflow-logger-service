package life.qbic.nextflow.weblog

import groovy.transform.EqualsAndHashCode
import java.text.SimpleDateFormat

enum NextflowEventType
{
    STARTED, PROCESS_SUBMITTED, PROCESS_STARTED, PROCESS_COMPLETED, ERROR, COMPLETED, UNKNOWN
}

@EqualsAndHashCode()
class RunInfo {

    static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    private NextflowEventType event

    private String name

    private String status

    private String id

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
        Date date
        try {
            date = new SimpleDateFormat(DATE_TIME_PATTERN).parse(s)
        } catch (Exception e) {
            date = new Date()
        }
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
