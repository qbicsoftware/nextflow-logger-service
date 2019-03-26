package life.qbic.weblog.nextflow

import org.codehaus.groovy.runtime.NullObject

class Trace {

    private Map traceInformation

    Trace(Map trace) {
        traceInformation = trace
    }

    @Override
    Object getProperty(String s) {
        return traceInformation.get(s) ?: NullObject
    }
}
