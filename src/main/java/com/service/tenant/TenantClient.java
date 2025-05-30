package com.service.tenant;

import com.service.config.PaymentClientProperties;
import com.service.exception.PaymentException;
import com.service.exception.TenantException;
import com.service.tenant.model.TenantActivationResponseDto;
import com.service.tenant.model.TenantDto;
import com.service.tenant.model.TenantRegistrationDto;
import com.service.tenant.model.TenantRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TenantClient {

    private final RestTemplate restTemplate;
    private final PaymentClientProperties properties;
    private final String baseUrl;

    /**
     * Creates a new TenantClient.
     *
     * @param restTemplate The RestTemplate to use for HTTP requests
     * @param properties   The configuration properties
     */
    public TenantClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.baseUrl = properties.getBaseUrl() + "/api/tenants";
    }

    /**
     * Creates a new pending tenant.
     *
     * @param request The tenant registration request
     * @param clientId The client ID for multi-tenant support
     * @return The ID of the created pending tenant
     * @throws TenantException if tenant creation fails
     */
    public UUID createPendingTenant(TenantRegistrationDto request, UUID clientId) {
        log.debug("Creating pending tenant with request: {} for client: {}", request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<TenantRegistrationDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<UUID> response = restTemplate.exchange(
                    baseUrl + "/pending",
                    HttpMethod.POST,
                    entity,
                    UUID.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("name already exists")) {
                    throw new TenantException("Tenant with this name already exists", e);
                } else if (responseBody.contains("name cannot be null")) {
                    throw new TenantException("Tenant name is required", e);
                }
                throw new TenantException("Invalid tenant data: " + responseBody, e);
            }
            throw handleHttpException(e, "Failed to create pending tenant");
        } catch (Exception e) {
            log.error("Error creating pending tenant", e);
            throw new TenantException("Failed to create pending tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Activates a pending tenant.
     *
     * @param pendingTenantId The ID of the pending tenant to activate
     * @param clientId The client ID for multi-tenant support
     * @return The tenant activation response with tenant code
     * @throws TenantException if activation fails
     */
    public TenantActivationResponseDto activateTenant(UUID pendingTenantId, UUID clientId) {
        log.debug("Activating tenant with ID: {} for client: {}", pendingTenantId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + pendingTenantId + "/activate",
                    HttpMethod.POST,
                    entity,
                    TenantActivationResponseDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new TenantException("Tenant not found with ID: " + pendingTenantId, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("not in pending payment state")) {
                    throw new TenantException("Tenant is not in pending payment state", e);
                }
                throw new TenantException("Invalid activation request: " + responseBody, e);
            }
            throw handleHttpException(e, "Failed to activate tenant");
        } catch (Exception e) {
            log.error("Error activating tenant with ID: {}", pendingTenantId, e);
            throw new TenantException("Failed to activate tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a tenant by its ID.
     *
     * @param tenantId The ID of the tenant to retrieve
     * @param clientId The client ID for multi-tenant support
     * @return The tenant details
     * @throws TenantException if the tenant doesn't exist
     */
    public TenantDto getTenant(UUID tenantId, UUID clientId) {
        log.debug("Fetching tenant with ID: {} for client: {}", tenantId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + tenantId,
                    HttpMethod.GET,
                    entity,
                    TenantDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new TenantException("Tenant not found with ID: " + tenantId, e);
            }
            throw handleHttpException(e, "Failed to retrieve tenant");
        } catch (Exception e) {
            log.error("Error retrieving tenant with ID: {}", tenantId, e);
            throw new TenantException("Failed to retrieve tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all tenants associated with the current client.
     *
     * @param clientId The client ID for multi-tenant support
     * @return A list of all tenants
     * @throws TenantException if retrieval fails
     */
    public List<TenantDto> getAllTenants(UUID clientId) {
        log.debug("Fetching all tenants for client: {}", clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            TenantDto[] tenants = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    TenantDto[].class
            ).getBody();
            return tenants != null ? Arrays.asList(tenants) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving all tenants for client: {}", clientId, e);
            throw new TenantException("Failed to retrieve tenants: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing tenant.
     *
     * @param tenantId The ID of the tenant to update
     * @param request The updated tenant details
     * @param clientId The client ID for multi-tenant support
     * @return The updated tenant
     * @throws TenantException if the tenant doesn't exist or update fails
     */
    public TenantDto updateTenant(UUID tenantId, TenantRequestDto request, UUID clientId) {
        log.debug("Updating tenant with ID: {} and request: {} for client: {}", tenantId, request, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<TenantRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + tenantId,
                    HttpMethod.PUT,
                    entity,
                    TenantDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new TenantException("Tenant not found with ID: " + tenantId, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("name already exists")) {
                    throw new TenantException("Tenant with this name already exists", e);
                }
                throw new TenantException("Invalid tenant data: " + responseBody, e);
            }
            throw handleHttpException(e, "Failed to update tenant");
        } catch (Exception e) {
            log.error("Error updating tenant with ID: {}", tenantId, e);
            throw new TenantException("Failed to update tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Deactivates an active tenant.
     *
     * @param tenantId The ID of the tenant to deactivate
     * @param clientId The client ID for multi-tenant support
     * @return The deactivated tenant details
     * @throws TenantException if deactivation fails
     */
    public TenantDto deactivateTenant(UUID tenantId, UUID clientId) {
        log.debug("Deactivating tenant with ID: {} for client: {}", tenantId, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/" + tenantId + "/deactivate",
                    HttpMethod.POST,
                    entity,
                    TenantDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new TenantException("Tenant not found with ID: " + tenantId, e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("already inactive")) {
                    throw new TenantException("Tenant is already inactive", e);
                }
                throw new TenantException("Cannot deactivate tenant: " + responseBody, e);
            }
            throw handleHttpException(e, "Failed to deactivate tenant");
        } catch (Exception e) {
            log.error("Error deactivating tenant with ID: {}", tenantId, e);
            throw new TenantException("Failed to deactivate tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a tenant exists by tenant code.
     *
     * @param tenantCode The tenant code to check
     * @return true if tenant exists, false otherwise
     * @throws TenantException if the check fails
     */
    public boolean existsByTenantCode(String tenantCode) {
        log.debug("Checking if tenant exists with code: {}", tenantCode);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/exists/code/" + tenantCode,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to check tenant existence");
        } catch (Exception e) {
            log.error("Error checking tenant existence for code: {}", tenantCode, e);
            throw new TenantException("Failed to check tenant existence: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a tenant code.
     *
     * @param tenantCode The tenant code to validate
     * @return true if tenant code is valid and active, false otherwise
     * @throws TenantException if validation fails
     */
    public boolean validateTenantCode(String tenantCode) {
        log.debug("Validating tenant code: {}", tenantCode);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    baseUrl + "/validate/code/" + tenantCode,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw handleHttpException(e, "Failed to validate tenant code");
        } catch (Exception e) {
            log.error("Error validating tenant code: {}", tenantCode, e);
            throw new TenantException("Failed to validate tenant code: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves tenant details by tenant code.
     *
     * @param tenantCode The tenant code
     * @param clientId The client ID for multi-tenant support
     * @return The tenant details
     * @throws TenantException if the tenant doesn't exist
     */
    public TenantDto getTenantByCode(String tenantCode, UUID clientId) {
        log.debug("Fetching tenant with code: {} for client: {}", tenantCode, clientId);
        HttpHeaders headers = createHeaders();
        headers.set("X-CLIENT-ID", clientId.toString());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/code/" + tenantCode,
                    HttpMethod.GET,
                    entity,
                    TenantDto.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new TenantException("Tenant not found with code: " + tenantCode, e);
            }
            throw handleHttpException(e, "Failed to retrieve tenant by code");
        } catch (Exception e) {
            log.error("Error retrieving tenant with code: {}", tenantCode, e);
            throw new TenantException("Failed to retrieve tenant by code: " + e.getMessage(), e);
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

        // Fix: Change X-Client-ID to X-CLIENT-ID (uppercase)
        if (properties.getClientId() != null) {
            headers.set("X-CLIENT-ID", properties.getClientId().toString());
        }

        return headers;
    }

    /**
     * Handles HTTP exceptions and converts them to TenantException with appropriate messages.
     *
     * @param e              The HTTP exception
     * @param defaultMessage The default error message
     * @return A new TenantException
     */
    private TenantException handleHttpException(HttpClientErrorException e, String defaultMessage) {
        log.debug("HTTP error status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());

        HttpStatusCode statusCode = e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();

        if (statusCode.equals(HttpStatus.BAD_REQUEST)) {
            return new TenantException("Invalid request data: " + responseBody, e);
        } else if (statusCode.equals(HttpStatus.UNAUTHORIZED)) {
            return new TenantException("Authentication failed - check API credentials", e);
        } else if (statusCode.equals(HttpStatus.FORBIDDEN)) {
            return new TenantException("Access denied - insufficient permissions", e);
        } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
            return new TenantException("Resource not found", e);
        } else if (statusCode.equals(HttpStatus.CONFLICT)) {
            return new TenantException("Resource conflict: " + responseBody, e);
        } else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
            return new TenantException("Validation failed: " + responseBody, e);
        } else if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return new TenantException("Server error occurred", e);
        } else if (statusCode.equals(HttpStatus.SERVICE_UNAVAILABLE)) {
            return new TenantException("Service temporarily unavailable", e);
        }

        return new TenantException(defaultMessage + ": " + responseBody, e);
    }
}