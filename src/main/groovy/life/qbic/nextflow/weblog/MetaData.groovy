package life.qbic.nextflow.weblog

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
}
