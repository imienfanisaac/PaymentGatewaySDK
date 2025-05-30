package com.service;

import com.service.account.AccountClient;
import com.service.account.model.TransferRequestDto;
import com.service.bank.BankClient;
import com.service.bank.model.BankRequestDto;
import com.service.card.CardClient;
import com.service.client.ClientClient;
import com.service.config.PaymentClientProperties;
import com.service.customer.CustomerClient;
import com.service.tenant.TenantClient;
import lombok.Getter;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Main client for the Payment Gateway API.
 * This class provides a centralized access point to all domain-specific clients.
 */
@Getter
public class PaymentGatewayClient {

    private final AccountClient accountClient;
    private final BankClient bankClient;
    private final CardClient cardClient;
    private final ClientClient clientClient;
    private final CustomerClient customerClient;
    private final TenantClient tenantClient;
    private final PaymentService paymentService;

    /**
     * Creates a new PaymentGatewayClient with the provided RestTemplate and properties.
     *
     * @param restTemplate The RestTemplate to use for HTTP communications
     * @param properties Configuration properties for the client
     */
    public PaymentGatewayClient(RestTemplate restTemplate, PaymentClientProperties properties) {
        this.accountClient = new AccountClient(restTemplate, properties);
        this.bankClient = new BankClient(restTemplate, properties);
        this.cardClient = new CardClient(restTemplate, properties);
        this.clientClient = new ClientClient(restTemplate, properties);
        this.customerClient = new CustomerClient(restTemplate, properties);
        this.tenantClient = new TenantClient(restTemplate, properties);
        this.paymentService = new PaymentService(restTemplate, properties);
    }

    /**
     * Process a payment (convenience method).
     * This is an example of a high-level operation that might involve multiple API calls.
     *
     * @param request The payment request
     * @return The payment response
     */
    public Object processPayment(Object request) {
        return paymentService.processPayment(request);
    }

    /**
     * Get account balance (convenience method).
     *
     * @param accountId The account ID
     * @return The account balance
     */
    public Object getAccountBalance(UUID accountId, UUID clientId) {
        return accountClient.getAccount(accountId, clientId);
    }

    /**
     * Create a new bank (convenience method).
     *
     * @param request The bank request
     * @return The created bank
     */
    public Object createBank(Object request) {
        return bankClient.createBank((BankRequestDto) request);
    }

    /**
     * Process a transfer between accounts (convenience method).
     *
     * @param request The transfer request
     * @return The transfer response
     */
    public Object processTransfer(Object request) {
        return accountClient.transferMoney((TransferRequestDto) request);
    }
}