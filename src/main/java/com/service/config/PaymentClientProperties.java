package com.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@Primary
@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentClientProperties {
    private String apiKey;
    private String baseUrl = "http://localhost:8080";
    private int connectiontimeout = 30000; // ms
    private boolean testMode = false;
    private UUID clientId;
    private int readTimeout = 30000;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectiontimeout() {
        return connectiontimeout;
    }

    public void setConnectiontimeout(int connectiontimeout) {
        this.connectiontimeout = connectiontimeout;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

}


