package life.qbic.model.weblog

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode()
class MetaData {

    private final Map metadata

    MetaData() {
        metadata = [:].asImmutable()
    }

    MetaData(Map metadata){
        this.metadata = metadata.asImmutable()
    }

    @Override
    Object getProperty(String s) {
        return this.metadata.get(s)
    }

    @Override
    boolean equals(Object o) {
        if( !o instanceof MetaData ) {
            return false
        }
        def trace = o as MetaData
        for( String key in this.metadata.keySet() ) {
            def thisProperty = this.getProperty(key)
            def otherProperty = trace.getProperty(key)
            if( ! otherProperty || ( otherProperty != thisProperty ) ) {
                return false
            }
        }
        return true
    }
}
