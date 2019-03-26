package life.qbic.weblog.nextflow

import java.text.SimpleDateFormat

enum NextflowEventType
{
    STARTED, PROCESS_SUBMITTED, PROCESS_STARTED, PROCESS_COMPLETED, ERROR, COMPLETED, UNKNOWN
}

class RunInfo {

    static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    private NextflowEventType eventType

    private String name, status

    private Date time

    RunInfo(){
        eventType = NextflowEventType.UNKNOWN
        name = ""
        status = ""
        time = new Date()
    }

    RunInfo(Map<String, String> weblogRunInformation){
        eventType = convertEventStringToEnum(weblogRunInformation.get('event'))
        name = weblogRunInformation.get('runName') ?: ""
        status = weblogRunInformation.get('runStatus') ?: ""
        time = new SimpleDateFormat(DATE_TIME_PATTERN).parse(weblogRunInformation.get('utcTime'))
    }

    private static convertEventStringToEnum(String event){
        try {
            def eventType = event.toUpperCase() as NextflowEventType
            return eventType
        } catch (IllegalArgumentException e){
            return NextflowEventType.UNKNOWN
        }
    }

}
