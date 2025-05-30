package com.service.card;

import com.service.card.model.CardDto;
import com.service.card.model.CardRequestDto;
import com.service.config.PaymentClientProperties;
import com.service.exception.PaymentException;
import com.service.account.model.TransferResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class CardClient {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;
    private static final String message = "CARD_ERROR";

    /**
     * Creates a new CardClient.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties   The configuration properties
     */
    public CardClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api/cards";
    }

    /**
     * Creates a new card.
     *
     * @param request The card creation request
     * @param clientId The client ID for multi-tenant support
     * @return The created card
     * @throws PaymentException if card creation fails
     */
    public CardDto createCard(CardRequestDto request, UUID clientId) {
        log.debug("Creating new card with request: {} for client: {}", request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<CardRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    CardDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Account not found")) {
                    throw new PaymentException("Account not found with the provided account number", message, e);
                }
                throw new PaymentException("Invalid card data: " + responseBody, message, e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Associated account not found", message, e);
            }
            throw handleHttpException(e, "Failed to create card");
        } catch (Exception e) {
            log.error("Error creating card", e);
            throw new PaymentException("Failed to create card: " + e.getMessage(), message, e);
        }
    }

    /**
     * Updates an existing card.
     *
     * @param cardId The ID of the card to update
     * @param request The updated card details
     * @param clientId The client ID for multi-tenant support
     * @return The updated card
     * @throws PaymentException if the card doesn't exist or update fails
     */
    public CardDto updateCard(UUID cardId, CardRequestDto request, UUID clientId) {
        log.debug("Updating card with ID: {} and request: {} for client: {}", cardId, request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<CardRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + cardId,
                    HttpMethod.PUT,
                    entity,
                    CardDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Card not found")) {
                    throw new PaymentException("Card not found with ID: " + cardId, message, e);
                } else if (responseBody.contains("Account not found")) {
                    throw new PaymentException("Associated account not found", message, e);
                }
                throw new PaymentException("Resource not found", message, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new PaymentException("Invalid card data: " + e.getResponseBodyAsString(), message, e);
            }
            throw handleHttpException(e, "Failed to update card");
        } catch (Exception e) {
            log.error("Error updating card with ID: {}", cardId, e);
            throw new PaymentException("Failed to update card: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves a card by its ID.
     *
     * @param cardId The ID of the card to retrieve
     * @param clientId The client ID for multi-tenant support
     * @return The card details
     * @throws PaymentException if the card doesn't exist
     */
    public CardDto getCard(UUID cardId, UUID clientId) {
        log.debug("Fetching card with ID: {} for client: {}", cardId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + cardId,
                    HttpMethod.GET,
                    entity,
                    CardDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Card not found with ID: " + cardId, message, e);
            }
            throw handleHttpException(e, "Failed to retrieve card");
        } catch (Exception e) {
            log.error("Error retrieving card with ID: {}", cardId, e);
            throw new PaymentException("Failed to retrieve card: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves all cards associated with the current client.
     *
     * @param clientId The client ID for multi-tenant support
     * @return A list of all cards
     * @throws PaymentException if retrieval fails
     */
    public List<CardDto> getAllCards(UUID clientId) {
        log.debug("Fetching all cards for client: {}", clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            CardDto[] cards = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    CardDto[].class
            ).getBody();
            return cards != null ? Arrays.asList(cards) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving all cards for client: {}", clientId, e);
            throw new PaymentException("Failed to retrieve cards: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves cards by account number.
     *
     * @param accountNo The account number
     * @param clientId The client ID for multi-tenant support
     * @return A list of cards associated with the account
     * @throws PaymentException if retrieval fails
     */
    public List<CardDto> getCardsByAccountNo(String accountNo, UUID clientId) {
        log.debug("Fetching cards for account number: {} and client: {}", accountNo, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            CardDto[] cards = restTemplate.exchange(
                    baseUrl + "/account/" + accountNo,
                    HttpMethod.GET,
                    entity,
                    CardDto[].class
            ).getBody();
            return cards != null ? Arrays.asList(cards) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Collections.emptyList();
            }
            throw handleHttpException(e, "Failed to retrieve cards by account number");
        } catch (Exception e) {
            log.error("Error retrieving cards for account number: {}", accountNo, e);
            throw new PaymentException("Failed to retrieve cards by account number: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves cards by card type.
     *
     * @param cardType The card type (e.g., DEBIT, CREDIT)
     * @param clientId The client ID for multi-tenant support
     * @return A list of cards of the specified type
     * @throws PaymentException if retrieval fails
     */
    public List<CardDto> getCardsByType(String cardType, UUID clientId) {
        log.debug("Fetching cards of type: {} for client: {}", cardType, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-Client-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            CardDto[] cards = restTemplate.exchange(
                    baseUrl + "/type/" + cardType,
                    HttpMethod.GET,
                    entity,
                    CardDto[].class
            ).getBody();
            return cards != null ? Arrays.asList(cards) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving cards of type: {}", cardType, e);
            throw new PaymentException("Failed to retrieve cards by type: " + e.getMessage(), message, e);
        }
    }

    /**
     * Checks if a card exists by ID and client.
     *
     * @param cardId The card ID
     * @param clientId The client ID for multi-tenant support
     * @return true if card exists, false otherwise
     * @throws PaymentException if the check fails
     */
    public boolean existsById(UUID cardId, UUID clientId) {
        log.debug("Checking if card exists with ID: {} for client: {}", cardId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/" + cardId + "/exists",
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to check card existence");
        } catch (Exception e) {
            log.error("Error checking card existence for ID: {}", cardId, e);
            throw new PaymentException("Failed to check card existence: " + e.getMessage(), message, e);
        }
    }

    /**
     * Transfers money using a card to an account.
     *
     * @param cardNo The card number
     * @param cvv The card CVV
     * @param toAccountNo The destination account number
     * @param amount The amount to transfer
     * @return The transfer response with transaction details
     * @throws PaymentException if transfer fails
     */
    public TransferResponseDto transferMoneyWithCard(String cardNo, String cvv, Long toAccountNo, BigDecimal amount) {
        log.debug("Processing card transfer from card: {} to account: {} amount: {}",
                cardNo.substring(0, 4) + "****", toAccountNo, amount);

        HttpHeaders headers = createHeaders();

        // Create transfer request
        Map<String, Object> transferRequest = new HashMap<>();
        transferRequest.put("cardNo", cardNo);
        transferRequest.put("cvv", cvv);
        transferRequest.put("toAccountNo", toAccountNo);
        transferRequest.put("amount", amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(transferRequest, headers);

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
                if (responseBody.contains("Invalid CVV")) {
                    throw new PaymentException("Invalid card CVV provided", message, e);
                } else if (responseBody.contains("Card has expired")) {
                    throw new PaymentException("Card has expired", message, e);
                } else if (responseBody.contains("insufficient balance")) {
                    throw new PaymentException("Insufficient balance on the card account", message, e);
                } else if (responseBody.contains("currency mismatch")) {
                    throw new PaymentException("Currency mismatch between source and destination accounts", message, e);
                }
                throw new PaymentException("Invalid transfer request: " + responseBody, message, e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Card not found")) {
                    throw new PaymentException("Card not found", message, e);
                } else if (responseBody.contains("Account not found")) {
                    throw new PaymentException("Destination account not found", message, e);
                }
                throw new PaymentException("Resource not found for transfer", message, e);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new PaymentException("Card authentication failed", message, e);
            }
            throw handleHttpException(e, "Failed to process card transfer");
        } catch (Exception e) {
            log.error("Error processing card transfer", e);
            throw new PaymentException("Failed to process card transfer: " + e.getMessage(), message, e);
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
