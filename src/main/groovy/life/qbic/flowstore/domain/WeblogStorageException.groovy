package life.qbic.flowstore.domain

class WeblogStorageException extends RuntimeException{

    WeblogStorageException() {
        super()
    }

    WeblogStorageException(String message) {
        super(message)
    }

    WeblogStorageException(String message, Throwable cause) {
        super(message, cause)
    }

}
