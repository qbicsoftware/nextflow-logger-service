package life.qbic.weblog.nextflow

import org.codehaus.groovy.runtime.NullObject


class MetaData {

    private Map metadata

    MetaData() {
        metadata = new HashMap()
    }

    MetaData(Map metadata){
        this.metadata = metadata
    }

    @Override
    Object getProperty(String s) {
        return this.metadata.get(s) ?: NullObject
    }
}
