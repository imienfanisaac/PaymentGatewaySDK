package com.service;


import com.service.config.PaymentClientProperties;
import com.service.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Service for general payment operations.
 * This service handles operations that don't fall cleanly into other domain clients.
 */
@Slf4j
public class PaymentService {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;
    private static final String message = "ERROR";

    /**
     * Creates a new PaymentService.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties The configuration properties
     */
    public PaymentService(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api";
    }

    /**
     * Process a payment.
     *
     * @param request The payment request
     * @return The payment response
     */
    public Object processPayment(Object request) {
        log.debug("Processing payment: {}", request);
        HttpHeaders headers = createHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/process",
                    HttpMethod.POST,
                    entity,
                    Object.class
            ).getBody();
        } catch (Exception e) {
            log.error("Error processing payment", e);
            throw new PaymentException("Failed to process payment: " + e.getMessage(), message, e);
        }
    }

    /**
     * Check the status of a payment.
     *
     * @param paymentId The payment ID
     * @return The payment status
     */
    public Object checkPaymentStatus(String paymentId) {
        log.debug("Checking payment status: {}", paymentId);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + paymentId + "/status",
                    HttpMethod.GET,
                    entity,
                    Object.class
            ).getBody();
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            throw new PaymentException("Failed to check payment status: " + e.getMessage(), message, e);
        }
    }

    /**
     * Creates HTTP headers for API requests.
     *
     * @return The HTTP headers
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add API key header for authentication
        if (properties.getApiKey() != null) {
            headers.set("X-API-Key", properties.getApiKey());
        }

        // Add client ID header if available
        if (properties.getClientId() != null) {
            headers.set("X-Client-ID", properties.getClientId().toString());
        }

        return headers;
    }
}