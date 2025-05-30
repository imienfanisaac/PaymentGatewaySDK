package com.service.exception;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

/**
 * Exception thrown when tenant-related operations fail.
 * This exception is used to wrap various tenant service errors and provide
 * meaningful error messages to the calling code.
 */
public class TenantException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new TenantException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public TenantException(String message) {
        super(message);
    }

    /**
     * Constructs a new TenantException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the cause of the exception (which is saved for later retrieval)
     */
    public TenantException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new TenantException with the specified cause.
     *
     * @param cause the cause of the exception (which is saved for later retrieval)
     */
    public TenantException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new TenantException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the cause of the exception (which is saved for later retrieval)
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    protected TenantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    public TenantException(String code, String message, HttpClientErrorException ex) {
    }
}