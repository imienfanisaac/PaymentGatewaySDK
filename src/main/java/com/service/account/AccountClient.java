package com.service.account;

import com.service.account.model.*;
import com.service.config.PaymentClientProperties;
import com.service.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Client for interacting with the Bank API endpoints.
 * Provides methods for creating, retrieving, and updating banks.
 */
@Slf4j
public class AccountClient {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;
    private static final String message = "ACCOUNT_ERROR";

    /**
     * Creates a new AccountClient.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties   The configuration properties
     */
    public AccountClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api/accounts";
    }

    /**
     * Creates a new account.
     *
     * @param request The account creation request
     * @param clientId The client ID for multi-tenant support
     * @return The created account
     * @throws PaymentException if account creation fails
     */
    public AccountDto createAccount(AccountRequestDto request, UUID clientId) {
        log.debug("Creating new account with request: {} for client: {}", request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<AccountRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    AccountDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Customer not found")) {
                    throw new PaymentException("Customer not found with the provided ID", message, e);
                } else if (responseBody.contains("Customer ID cannot be null")) {
                    throw new PaymentException("Customer ID is required for account creation", message, e);
                } else if (responseBody.contains("account number already exists")) {
                    throw new PaymentException("Account with this number already exists", message, e);
                }
                throw new PaymentException("Invalid account data: " + responseBody, message, e);
            }
            throw handleHttpException(e, "Failed to create account");
        } catch (Exception e) {
            log.error("Error creating account", e);
            throw new PaymentException("Failed to create account: " + e.getMessage(), message, e);
        }
    }

    /**
     * Updates an existing account.
     *
     * @param accountId  The ID of the account to update
     * @param request The updated account details
     * @param clientId The client ID for multi-tenant support
     * @return The updated account
     * @throws PaymentException if the account doesn't exist or update fails
     */
    public AccountDto updateAccount(UUID accountId, AccountRequestDto request, UUID clientId) {
        log.debug("Updating account with ID: {} and request: {} for client: {}", accountId, request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<AccountRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + accountId,
                    HttpMethod.PUT,
                    entity,
                    AccountDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Account not found with ID: " + accountId, message, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Customer not found")) {
                    throw new PaymentException("Customer not found with the provided ID", message, e);
                }
                throw new PaymentException("Invalid account data: " + responseBody, message, e);
            }
            throw handleHttpException(e, "Failed to update account");
        } catch (Exception e) {
            log.error("Error updating account with ID: {}", accountId, e);
            throw new PaymentException("Failed to update account: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param accountId The ID of the account to retrieve
     * @param clientId The client ID for multi-tenant support
     * @return The account details
     * @throws PaymentException if the account doesn't exist
     */
    public AccountDto getAccount(UUID accountId, UUID clientId) {
        log.debug("Fetching account with ID: {} for client: {}", accountId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + accountId,
                    HttpMethod.GET,
                    entity,
                    AccountDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Account not found with ID: " + accountId, message, e);
            }
            throw handleHttpException(e, "Failed to retrieve account");
        } catch (Exception e) {
            log.error("Error retrieving account with ID: {}", accountId, e);
            throw new PaymentException("Failed to retrieve account: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves all accounts associated with the current client.
     *
     * @param clientId The client ID for multi-tenant support
     * @return A list of all accounts
     * @throws PaymentException if retrieval fails
     */
    public List<AccountDto> getAllAccounts(UUID clientId) {
        log.debug("Fetching all accounts for client: {}", clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            AccountDto[] accounts = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    AccountDto[].class
            ).getBody();
            return accounts != null ? Arrays.asList(accounts) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving all accounts for client: {}", clientId, e);
            throw new PaymentException("Failed to retrieve accounts: " + e.getMessage(), message, e);
        }
    }


    /**
     * Transfers money between accounts.
     *
     * @param request The transfer request
     * @return The transfer response with transaction details
     * @throws PaymentException if transfer fails
     */
    public TransferResponseDto transferMoney(TransferRequestDto request) {
        log.debug("Processing transfer request: {}", request);
        HttpHeaders headers = createHeaders();
        HttpEntity<TransferRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/transfer",
                    HttpMethod.POST,
                    entity,
                    TransferResponseDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("same account")) {
                    throw new PaymentException("Cannot transfer to the same account", message, e);
                } else if (responseBody.contains("insufficient balance")) {
                    throw new PaymentException("Insufficient balance for transfer", message, e);
                } else if (responseBody.contains("currency mismatch")) {
                    throw new PaymentException("Currency mismatch between accounts", message, e);
                }
                throw new PaymentException("Invalid transfer request: " + responseBody, message, e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("One or both accounts not found", message, e);
            }
            throw handleHttpException(e, "Failed to process transfer");
        } catch (Exception e) {
            log.error("Error processing transfer", e);
            throw new PaymentException("Failed to process transfer: " + e.getMessage(), message, e);
        }
    }

    /**
     * Checks if an account exists by account number.
     *
     * @param accountNo The account number to check
     * @return true if account exists, false otherwise
     * @throws PaymentException if the check fails
     */
    public boolean existsByAccountNo(Long accountNo) {
        log.debug("Checking if account exists with number: {}", accountNo);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/exists/" + accountNo,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to check account existence");
        } catch (Exception e) {
            log.error("Error checking account existence for number: {}", accountNo, e);
            throw new PaymentException("Failed to check account existence: " + e.getMessage(), message, e);
        }
    }

    /**
     * Confirms if an account exists by account number.
     *
     * @param accountNo The account number to confirm
     * @return true if account exists, false otherwise
     * @throws PaymentException if the confirmation fails
     */
    public boolean confirmAccount(Long accountNo) {
        log.debug("Confirming account with number: {}", accountNo);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/confirm/" + accountNo,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to confirm account");
        } catch (Exception e) {
            log.error("Error confirming account with number: {}", accountNo, e);
            throw new PaymentException("Failed to confirm account: " + e.getMessage(), message, e);
        }
    }



    /**
     * Gets the balance for a specific account.
     *
     * @param accountNo The account number
     * @param clientId The client ID for multi-tenant support
     * @return The balance response with account details
     * @throws PaymentException if retrieval fails
     */
    public BalanceResponseDto getAccountBalance(Long accountNo, UUID clientId) {
        log.debug("Fetching balance for account: {} and client: {}", accountNo, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + accountNo + "/balance",
                    HttpMethod.GET,
                    entity,
                    BalanceResponseDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Account not found with number: " + accountNo, message, e);
            }
            throw handleHttpException(e, "Failed to retrieve account balance");
        } catch (Exception e) {
            log.error("Error retrieving balance for account: {}", accountNo, e);
            throw new PaymentException("Failed to retrieve account balance: " + e.getMessage(), message, e);
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
