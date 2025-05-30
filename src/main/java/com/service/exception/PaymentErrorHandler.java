package com.service.exception;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

public class PaymentErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        try {
            super.handleError(response);
        }  catch (HttpClientErrorException ex) {
            // Use HTTP status code and response body directly
            String code = ex.getStatusCode().toString();
            String message = ex.getResponseBodyAsString();

            throw new PaymentException(code, message, ex);
        }
    }


}
