package com.service.exception;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

public class TenantErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        try {
            super.handleError(response);
        } catch (HttpClientErrorException ex) {
            // DEBUG: Print the actual error details
            System.out.println("=== HTTP Error Details ===");
            System.out.println("Status Code: " + ex.getStatusCode());
            System.out.println("Response Body: " + ex.getResponseBodyAsString());
            System.out.println("Headers: " + ex.getResponseHeaders());
            System.out.println("=== End Error Details ===");

            String code = ex.getStatusCode().toString();
            String message = ex.getResponseBodyAsString();

            throw new TenantException(code, message, ex);
        }
    }
}