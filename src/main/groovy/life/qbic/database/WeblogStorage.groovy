package life.qbic.database

import life.qbic.nextflow.WeblogMessage

interface WeblogStorage {

    void storeWeblogMessage(WeblogMessage message) throws WeblogStorageException

}
