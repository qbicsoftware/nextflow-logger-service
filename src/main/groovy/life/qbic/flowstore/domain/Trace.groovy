package life.qbic.flowstore.domain

import com.fasterxml.jackson.annotation.JsonProperty


class Trace {

    @JsonProperty("properties")
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

    @Override
    boolean equals(Object o) {
        if( !o instanceof Trace ) {
            return false
        }
        def trace = o as Trace
        for( String key in this.traceInformation.keySet() ) {
            def thisProperty = this.getProperty(key)
            def otherProperty = trace.getProperty(key)
            if( ! otherProperty || ( otherProperty != thisProperty ) ) {
                return false
            }
        }
        return true
    }
}
