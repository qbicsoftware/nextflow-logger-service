package life.qbic.nextflow.weblog

import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.runtime.NullObject

@EqualsAndHashCode()
class MetaData {

    private def metadata

    MetaData() {
        metadata = [:]
    }

    MetaData(Map metadata){
        this.metadata = metadata
    }

    @Override
    Object getProperty(String s) {
        return this.metadata.get(s) ?: NullObject
    }
}
