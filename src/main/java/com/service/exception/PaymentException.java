package com.service.exception;

public class PaymentException extends RuntimeException {
    // Other fields and constructors...

    /**
     * Creates a new PaymentException with the specified message and cause.
     *
     * @param message the error message
     * @param s
     * @param cause   the cause exception
     */
    public PaymentException(String message, String s, Throwable cause) {
        super(message, cause);
    }
}
