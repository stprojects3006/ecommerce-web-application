package com.queue_it.connector;

public class KnownUserException extends Exception {
    /**
     * The serializable class KnowUserException does not declare a static final
     * serialVersionUID field of type long
     */
    private static final long serialVersionUID = 1L;

    public KnownUserException(String message) {
        super(message);
    }

    public KnownUserException(String message, Throwable thrwbl) {
        super(message, thrwbl);
    }
}