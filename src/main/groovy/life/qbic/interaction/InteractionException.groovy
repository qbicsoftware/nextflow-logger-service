package life.qbic.interaction

class InteractionException extends RuntimeException {

    InteractionException() {
        super()
    }

    InteractionException(String message) {
        super(message)
    }

    InteractionException(String message, Throwable cause) {
        super(message, cause)
    }
}
