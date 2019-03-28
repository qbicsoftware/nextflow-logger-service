package life.qbic.nextflow.weblog

import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.runtime.NullObject

@EqualsAndHashCode()
class Trace {

    private Map traceInformation

    Trace() {
        traceInformation = [:]
    }

    Trace(Map trace) {
        traceInformation = trace
    }

    @Override
    Object getProperty(String s) {
        return traceInformation.get(s) ?: NullObject
    }
}
