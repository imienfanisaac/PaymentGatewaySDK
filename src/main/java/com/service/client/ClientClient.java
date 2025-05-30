package com.service.client;
import com.service.client.model.ClientDto;
import com.service.client.model.ClientRequestDto;
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
@Slf4j
public class ClientClient {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;
    private static final String message = "CLIENT_ERROR";

    /**
     * Creates a new ClientClient.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties   The configuration properties
     */
    public ClientClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api/clients";
    }

    /**
     * Creates a new client.
     *
     * @param request The client creation request
     * @param tenantId The tenant ID for multi-tenant support
     * @return The created client
     * @throws PaymentException if client creation fails
     */
    public ClientDto createClient(ClientRequestDto request, UUID tenantId) {
        log.debug("Creating new client with request: {} for tenant: {}", request, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<ClientRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    ClientDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Tenant not found")) {
                    throw new PaymentException("Tenant not found with the provided ID", message, e);
                }
                throw new PaymentException("Invalid client data: " + responseBody, message, e);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Associated tenant not found", message, e);
            }
            throw handleHttpException(e, "Failed to create client");
        } catch (Exception e) {
            log.error("Error creating client", e);
            throw new PaymentException("Failed to create client: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves a client by its ID.
     *
     * @param clientId The ID of the client to retrieve
     * @param tenantId The tenant ID for multi-tenant support
     * @return The client details
     * @throws PaymentException if the client doesn't exist
     */
    public ClientDto getClient(UUID clientId, UUID tenantId) {
        log.debug("Fetching client with ID: {} for tenant: {}", clientId, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-Tenant-ID", tenantId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + clientId,
                    HttpMethod.GET,
                    entity,
                    ClientDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Client not found with ID: " + clientId, message, e);
            }
            throw handleHttpException(e, "Failed to retrieve client");
        } catch (Exception e) {
            log.error("Error retrieving client with ID: {}", clientId, e);
            throw new PaymentException("Failed to retrieve client: " + e.getMessage(), message, e);
        }
    }

    /**
     * Retrieves all clients associated with the specified tenant.
     *
     * @param tenantId The tenant ID for multi-tenant support
     * @return A list of all clients for the tenant
     * @throws PaymentException if retrieval fails
     */
    public List<ClientDto> getAllClientsByTenant(UUID tenantId) {
        log.debug("Fetching all clients for tenant: {}", tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ClientDto[] clients = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    ClientDto[].class
            ).getBody();
            return clients != null ? Arrays.asList(clients) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Tenant not found")) {
                    throw new PaymentException("Tenant not found with ID: " + tenantId, message, e);
                }
                return Collections.emptyList();
            }
            throw handleHttpException(e, "Failed to retrieve clients by tenant");
        } catch (Exception e) {
            log.error("Error retrieving all clients for tenant: {}", tenantId, e);
            throw new PaymentException("Failed to retrieve clients: " + e.getMessage(), message, e);
        }
    }

    /**
     * Updates an existing client.
     *
     * @param clientId The ID of the client to update
     * @param request The updated client details
     * @param tenantId The tenant ID for multi-tenant support
     * @return The updated client
     * @throws PaymentException if the client doesn't exist or update fails
     */
    public ClientDto updateClient(UUID clientId, ClientRequestDto request, UUID tenantId) {
        log.debug("Updating client with ID: {} and request: {} for tenant: {}", clientId, request, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<ClientRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + clientId,
                    HttpMethod.PUT,
                    entity,
                    ClientDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("Client not found")) {
                    throw new PaymentException("Client not found with ID: " + clientId, message, e);
                } else if (responseBody.contains("Tenant not found")) {
                    throw new PaymentException("Associated tenant not found", message, e);
                }
                throw new PaymentException("Resource not found", message, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new PaymentException("Invalid client data: " + e.getResponseBodyAsString(), message, e);
            }
            throw handleHttpException(e, "Failed to update client");
        } catch (Exception e) {
            log.error("Error updating client with ID: {}", clientId, e);
            throw new PaymentException("Failed to update client: " + e.getMessage(), message, e);
        }
    }

    /**
     * Deletes a client.
     *
     * @param clientId The ID of the client to delete
     * @param tenantId The tenant ID for multi-tenant support
     * @throws PaymentException if the client doesn't exist or deletion fails
     */
    public void deleteClient(UUID clientId, UUID tenantId) {
        log.debug("Deleting client with ID: {} for tenant: {}", clientId, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    baseUrl + "/" + clientId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            log.debug("Client deleted successfully with ID: {}", clientId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Client not found with ID: " + clientId, message, e);
            } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new PaymentException("Cannot delete client - has dependent resources", message, e);
            }
            throw handleHttpException(e, "Failed to delete client");
        } catch (Exception e) {
            log.error("Error deleting client with ID: {}", clientId, e);
            throw new PaymentException("Failed to delete client: " + e.getMessage(), message, e);
        }
    }

    /**
     * Checks if a client exists by ID and tenant.
     *
     * @param clientId The client ID
     * @param tenantId The tenant ID for multi-tenant support
     * @return true if client exists, false otherwise
     * @throws PaymentException if the check fails
     */
    public boolean existsById(UUID clientId, UUID tenantId) {
        log.debug("Checking if client exists with ID: {} for tenant: {}", clientId, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/" + clientId + "/exists",
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to check client existence");
        } catch (Exception e) {
            log.error("Error checking client existence for ID: {}", clientId, e);
            throw new PaymentException("Failed to check client existence: " + e.getMessage(), message, e);
        }
    }

    /**
     * Activates a client.
     *
     * @param clientId The client ID to activate
     * @param tenantId The tenant ID for multi-tenant support
     * @return The updated client
     * @throws PaymentException if activation fails
     */
    public ClientDto activateClient(UUID clientId, UUID tenantId) {
        log.debug("Activating client with ID: {} for tenant: {}", clientId, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + clientId + "/activate",
                    HttpMethod.PUT,
                    entity,
                    ClientDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Client not found with ID: " + clientId, message, e);
            }
            throw handleHttpException(e, "Failed to activate client");
        } catch (Exception e) {
            log.error("Error activating client with ID: {}", clientId, e);
            throw new PaymentException("Failed to activate client: " + e.getMessage(), message, e);
        }
    }

    /**
     * Deactivates a client.
     *
     * @param clientId The client ID to deactivate
     * @param tenantId The tenant ID for multi-tenant support
     * @return The updated client
     * @throws PaymentException if deactivation fails
     */
    public ClientDto deactivateClient(UUID clientId, UUID tenantId) {
        log.debug("Deactivating client with ID: {} for tenant: {}", clientId, tenantId);
        HttpHeaders headers = createHeaders();
        headers.set("X-TENANT-ID", tenantId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + clientId + "/deactivate",
                    HttpMethod.PUT,
                    entity,
                    ClientDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Client not found with ID: " + clientId, message, e);
            }
            throw handleHttpException(e, "Failed to deactivate client");
        } catch (Exception e) {
            log.error("Error deactivating client with ID: {}", clientId, e);
            throw new PaymentException("Failed to deactivate client: " + e.getMessage(), message, e);
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