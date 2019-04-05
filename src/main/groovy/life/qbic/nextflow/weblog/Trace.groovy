package life.qbic.nextflow.weblog

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode()
class Trace {

    private final Map traceInformation

    Trace() {
        traceInformation = [:].asImmutable()
    }

    Trace(Map trace) {
        traceInformation = trace.asImmutable()
    }

    @Override
    Object getProperty(String s) {
        return traceInformation.get(s)
    }
}
