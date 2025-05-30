package com.service.customer;

import com.service.config.PaymentClientProperties;
import com.service.customer.model.CustomerDto;
import com.service.customer.model.CustomerRequestDto;
import com.service.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;
    private static final String message = "CUSTOMER_ERROR";

    /**
     * Creates a new CustomerClient.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties   The configuration properties
     */
    public CustomerClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api/customers";
    }

    /**
     * Creates a new customer.
     *
     * @param request The customer creation request
     * @param clientId The client ID for multi-tenant support
     * @return The created customer
     * @throws PaymentException if customer creation fails
     */
    public CustomerDto createCustomer(CustomerRequestDto request, UUID clientId) {
        log.debug("Creating new customer with request: {} for client: {}", request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<CustomerRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    CustomerDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Bank not found")) {
                    throw new PaymentException("Bank not found with the provided ID", message, e);
                }
                throw new PaymentException("Invalid customer data: " + responseBody, message, e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Associated bank not found", message, e);
            }
            throw handleHttpException(e, "Failed to create customer");
        } catch (Exception e) {
            log.error("Error creating customer", e);
            throw new PaymentException("Failed to create customer: " + e.getMessage(), message, e);
        }
    }

    /**
     * Updates an existing customer.
     *
     * @param customerId The ID of the customer to update
     * @param request The updated customer details
     * @param clientId The client ID for multi-tenant support
     * @return The updated customer
     * @throws PaymentException if the customer doesn't exist or update fails
     */
    public CustomerDto updateCustomer(UUID customerId, CustomerRequestDto request, UUID clientId) {
        log.debug("Updating customer with ID: {} and request: {} for client: {}", customerId, request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<CustomerRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + customerId,
                    HttpMethod.PUT,
                    entity,
                    CustomerDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Customer not found")) {
                    throw new PaymentException("Customer not found with ID: " + customerId, message, e);
                } else if (responseBody.contains("Bank not found")) {
                    throw new PaymentException("Associated bank not found", message, e);
                }
                throw new PaymentException("Resource not found", message, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new PaymentException("Invalid customer data: " + e.getResponseBodyAsString(), message, e);
            }
            throw handleHttpException(e, "Failed to update customer");
        } catch (Exception e) {
            log.error("Error updating customer with ID: {}", customerId, e);
            throw new PaymentException("Failed to update customer: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves a customer by its ID.
     *
     * @param customerId The ID of the customer to retrieve
     * @param clientId The client ID for multi-tenant support
     * @return The customer details
     * @throws PaymentException if the customer doesn't exist
     */
    public CustomerDto getCustomer(UUID customerId, UUID clientId) {
        log.debug("Fetching customer with ID: {} for client: {}", customerId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + customerId,
                    HttpMethod.GET,
                    entity,
                    CustomerDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Customer not found with ID: " + customerId, message, e);
            }
            throw handleHttpException(e, "Failed to retrieve customer");
        } catch (Exception e) {
            log.error("Error retrieving customer with ID: {}", customerId, e);
            throw new PaymentException("Failed to retrieve customer: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves all customers associated with the current client.
     *
     * @param clientId The client ID for multi-tenant support
     * @return A list of all customers
     * @throws PaymentException if retrieval fails
     */
    public List<CustomerDto> getAllCustomers(UUID clientId) {
        log.debug("Fetching all customers for client: {}", clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            CustomerDto[] customers = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    CustomerDto[].class
            ).getBody();
            return customers != null ? Arrays.asList(customers) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving all customers for client: {}", clientId, e);
            throw new PaymentException("Failed to retrieve customers: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves customers by bank ID.
     *
     * @param bankId The bank ID
     * @param clientId The client ID for multi-tenant support
     * @return A list of customers associated with the bank
     * @throws PaymentException if retrieval fails
     */
    public List<CustomerDto> getCustomersByBankId(UUID bankId, UUID clientId) {
        log.debug("Fetching customers for bank ID: {} and client: {}", bankId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            CustomerDto[] customers = restTemplate.exchange(
                    baseUrl + "/bank/" + bankId,
                    HttpMethod.GET,
                    entity,
                    CustomerDto[].class
            ).getBody();
            return customers != null ? Arrays.asList(customers) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Collections.emptyList();
            }
            throw handleHttpException(e, "Failed to retrieve customers by bank ID");
        } catch (Exception e) {
            log.error("Error retrieving customers for bank ID: {}", bankId, e);
            throw new PaymentException("Failed to retrieve customers by bank ID: " + e.getMessage(), message, e);
        }
    }

    /**
     * Checks if a customer exists by ID.
     *
     * @param customerId The customer ID
     * @return true if customer exists, false otherwise
     * @throws PaymentException if the check fails
     */
    public boolean existsById(UUID customerId) {
        log.debug("Checking if customer exists with ID: {}", customerId);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/" + customerId + "/exists",
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to check customer existence");
        } catch (Exception e) {
            log.error("Error checking customer existence for ID: {}", customerId, e);
            throw new PaymentException("Failed to check customer existence: " + e.getMessage(), message, e);
        }
    }

    /**
     * Creates HTTP headers for API requests.
     * Adds authentication and content type headers.
     *
     * @return The HTTP headers
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add API key header for authentication
        if (properties.getApiKey() != null) {
            headers.set("X-API-KEY", properties.getApiKey());  // Changed to uppercase KEY
        }

        // Fix: Change X-Client-ID to X-CLIENT-ID (uppercase)
        if (properties.getClientId() != null) {
            headers.set("X-CLIENT-ID", properties.getClientId().toString());  // Changed to uppercase ID
        }

        return headers;
    }

    /**
     * Handles HTTP exceptions and converts them to PaymentException with appropriate messages.
     *
     * @param e              The HTTP exception
     * @param defaultMessage The default error message
     * @return A new PaymentException
     */
    private PaymentException handleHttpException(HttpClientErrorException e, String defaultMessage) {
        log.debug("HTTP error status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());

        HttpStatusCode statusCode = e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();

        if (statusCode.equals(HttpStatus.BAD_REQUEST)) {
            return new PaymentException("Invalid request data: " + responseBody, message, e);
        } else if (statusCode.equals(HttpStatus.UNAUTHORIZED)) {
            return new PaymentException("Authentication failed - check API credentials", message, e);
        } else if (statusCode.equals(HttpStatus.FORBIDDEN)) {
            return new PaymentException("Access denied - insufficient permissions", message, e);
        } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
            return new PaymentException("Resource not found", message, e);
        } else if (statusCode.equals(HttpStatus.CONFLICT)) {
            return new PaymentException("Resource conflict: " + responseBody, message, e);
        } else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
            return new PaymentException("Validation failed: " + responseBody, message, e);
        } else if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return new PaymentException("Server error occurred", message, e);
        } else if (statusCode.equals(HttpStatus.SERVICE_UNAVAILABLE)) {
            return new PaymentException("Service temporarily unavailable", message, e);
        }

        return new PaymentException(defaultMessage + ": " + responseBody, message, e);
    }
}
