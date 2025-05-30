package com.service.bank;

import com.service.bank.model.BankDto;
import com.service.bank.model.BankRequestDto;
import com.service.config.PaymentClientProperties;
import com.service.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

/**
 * Client for interacting with the Bank API endpoints.
 * Provides methods for creating, retrieving, and updating banks.
 */
@Slf4j
public class BankClient {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;
    private static final String message = "BANK_ERROR";

    /**
     * Creates a new BankClient.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties The configuration properties
     */
    public BankClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api/banks";
    }

    /**
     * Creates a new bank.
     *
     * @param request The bank creation request containing sort code, name, and country
     * @return The created bank
     * @throws PaymentException if bank creation fails
     */
    public BankDto createBank(BankRequestDto request) {
        log.debug("Creating new bank with request: {}", request);
        HttpHeaders headers = createHeaders();
        HttpEntity<BankRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    BankDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == BAD_REQUEST &&
                    e.getResponseBodyAsString().contains("sort code already exists")) {
                throw new PaymentException("Bank with this sort code already exists", message, e);
            }
            throw handleHttpException(e, "Failed to create bank");
        } catch (Exception e) {
            log.error("Error creating bank", e);
            throw new PaymentException("Failed to create bank: " + e.getMessage(), message, e);
        }
    }

    /**
     * Updates an existing bank.
     *
     * @param bankId The ID of the bank to update
     * @param request The updated bank details
     * @return The updated bank
     * @throws PaymentException if the bank doesn't exist or update fails
     */
    public BankDto updateBank(UUID bankId, BankRequestDto request) {
        log.debug("Updating bank with ID: {} and request: {}", bankId, request);
        HttpHeaders headers = createHeaders();
        HttpEntity<BankRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + bankId,
                    HttpMethod.PUT,
                    entity,
                    BankDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Bank not found with ID: " + bankId, message, e);
            }
            throw handleHttpException(e, "Failed to update bank");
        } catch (Exception e) {
            log.error("Error updating bank with ID: {}", bankId, e);
            throw new PaymentException("Failed to update bank: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves a bank by its ID.
     *
     * @param bankId The ID of the bank to retrieve
     * @return The bank details
     * @throws PaymentException if the bank doesn't exist
     */
    public BankDto getBank(UUID bankId) {
        log.debug("Fetching bank with ID: {}", bankId);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + bankId,
                    HttpMethod.GET,
                    entity,
                    BankDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Bank not found with ID: " + bankId, message, e);
            }
            throw handleHttpException(e, "Failed to retrieve bank");
        } catch (Exception e) {
            log.error("Error retrieving bank with ID: {}", bankId, e);
            throw new PaymentException("Failed to retrieve bank: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves all banks associated with the current client.
     *
     * @return A list of all banks
     * @throws PaymentException if retrieval fails
     */
    public List<BankDto> getBanks() {
        log.debug("Fetching all banks");
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return Arrays.asList(restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    BankDto[].class
            ).getBody());
        } catch (Exception e) {
            log.error("Error retrieving all banks", e);
            throw new PaymentException("Failed to retrieve banks: " + e.getMessage(), message, e);
        }
    }

    /**
     * Checks if a bank with the given sort code exists.
     *
     * @param sortCode The bank sort code to check
     * @return true if a bank with the sort code exists, false otherwise
     */
    public boolean checkSortCodeExists(String sortCode) {
        log.debug("Checking if sort code exists: {}", sortCode);
        try {
            // Using getAllBanks and filtering client-side because
            // I don't see a dedicated endpoint for this in your controller
            return getBanks().stream()
                    .anyMatch(bank -> bank.getSortCode().equals(sortCode));
        } catch (Exception e) {
            log.error("Error checking sort code existence: {}", sortCode, e);
            throw new PaymentException("Failed to check sort code: " + e.getMessage(), message, e);
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

        if (properties.getApiKey() != null) {
            headers.set("X-API-KEY", properties.getApiKey());
            System.out.println("=== SDK Headers Debug ===");
            System.out.println("API Key from properties: " + properties.getApiKey());
            System.out.println("Setting header X-API-KEY: " + properties.getApiKey());
        } else {
            System.out.println("=== API KEY IS NULL! ===");
        }

        // Add client ID header if available
        if (properties.getClientId() != null) {
            headers.set("X-CLIENT-ID", properties.getClientId().toString());
            System.out.println("Setting header X-CLIENT-ID: " + properties.getClientId().toString());
        } else {
            System.out.println("=== CLIENT ID IS NULL! ===");
        }

        // Print all headers being sent
        System.out.println("All headers:");
        headers.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        System.out.println("=== End SDK Headers Debug ===");

        return headers;

        // Fix: Change X-API-Key to X-API-KEY (uppercase)
     /*   if (properties.getApiKey() != null) {
            headers.set("X-API-KEY", properties.getApiKey());  // Changed to uppercase KEY
        }

        // Fix: Change X-Client-ID to X-CLIENT-ID (uppercase)
        if (properties.getClientId() != null) {
            headers.set("X-CLIENT-ID", properties.getClientId().toString());  // Changed to uppercase ID
        }*/


    }

    /**
     * Handles HTTP exceptions and converts them to PaymentException with appropriate messages.
     *
     * @param e The HTTP exception
     * @param defaultMessage The default error message
     * @return A new PaymentException
     */
    private PaymentException handleHttpException(HttpClientErrorException e, String defaultMessage) {
        log.debug("HTTP error status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());

        HttpStatusCode statusCode = e.getStatusCode();
        if (statusCode.equals(BAD_REQUEST)) {
            return new PaymentException("Invalid bank data: " + e.getResponseBodyAsString(), message, e);
        } else if (statusCode.equals(FORBIDDEN)) {
            return new PaymentException("Access denied to bank resource", message, e);
        } else if (statusCode.equals(UNAUTHORIZED)) {
            return new PaymentException("Authentication failed", message, e);
        } else if (statusCode.equals(NOT_FOUND)) {
            return new PaymentException("Bank resource not found", message, e);
        }
        return new PaymentException(defaultMessage, message, e);
    }
}