package life.qbic.database

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
