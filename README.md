# Payment Gateway SDK

A comprehensive Java SDK for seamless integration with Payment Gateway APIs, providing a simple and intuitive way to manage tenants, clients, banks, accounts, and financial transactions.

## Overview

The Payment Gateway SDK is a robust, production-ready Java library that simplifies integration with payment gateway services. Built with Spring Boot and designed for enterprise use, this SDK provides a clean, type-safe API for managing multi-tenant payment operations.

Whether you're building a fintech application, e-commerce platform, or any system that requires payment processing capabilities, this SDK abstracts away the complexity of direct API calls and provides a developer-friendly interface for all payment gateway operations.

## Features

### 🏢 Multi-Tenant Architecture
- **Tenant Management**: Create, activate, and manage tenant accounts
- **Client Management**: Handle multiple clients per tenant with proper isolation
- **Hierarchical Access Control**: Tenant → Client → Resource structure

### 🏦 Banking Operations
- **Bank Management**: Create and manage bank entities
- **Account Operations**: Full CRUD operations for bank accounts
- **Balance Inquiries**: Real-time account balance retrieval
- **Money Transfers**: Secure inter-account transfers with validation

### 🔐 Security & Authentication
- **API Key Authentication**: Secure authentication using tenant-specific API keys
- **Client-based Authorization**: Resource-level access control
- **Header-based Routing**: Proper multi-tenant request routing

### ⚡ Developer Experience
- **Spring Boot Integration**: Auto-configuration and dependency injection
- **Type-Safe API**: Strongly typed DTOs and response objects
- **Comprehensive Error Handling**: Detailed error messages and exception handling
- **Flexible Configuration**: Easy configuration through application properties

### 🛠 Production Ready
- **Retry Logic**: Built-in handling for network failures
- **Timeout Configuration**: Configurable connection and read timeouts
- **Logging Integration**: SLF4J logging for debugging and monitoring
- **Maven Central**: Available as a standard Maven dependency

## Quick Start

### Installation

Add the SDK to your Maven project:

```xml
<dependency>
    <groupId>com.yourcompany</groupId>
    <artifactId>payment-gateway-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration

Configure the SDK in your `application.properties`:

```properties
payment.gateway.base-url=https://api.your-payment-gateway.com
payment.gateway.api-key=your-api-key
payment.gateway.client-id=your-client-id
```

### Basic Usage

```java
@Autowired
private PaymentGatewayClient paymentClient;

// Manage banks
List<BankDto> banks = paymentClient.getBankClient().getBanks();
BankDto newBank = paymentClient.getBankClient().createBank(bankRequest);

// Manage accounts
List<AccountDto> accounts = paymentClient.getAccountClient().getAllAccounts(clientId);
AccountDto account = paymentClient.getAccountClient().createAccount(accountRequest, clientId);

// Transfer money
TransferResponseDto transfer = paymentClient.getAccountClient().transferMoney(transferRequest);

// Manage tenants and clients
List<TenantDto> tenants = paymentClient.getTenantClient().getAllTenants(clientId);
ClientDto client = paymentClient.getClientClient().createClient(clientRequest, tenantId);
```

## Architecture

The SDK follows a hierarchical structure that mirrors typical payment gateway architectures:

```
Tenant (API Key holder)
├── Client (Business entity)
    ├── Banks
    ├── Accounts
    ├── Transactions
    └── Other Resources
```

## Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `payment.gateway.base-url` | Payment Gateway API base URL | `http://localhost:8080` |
| `payment.gateway.api-key` | Tenant API key for authentication | - |
| `payment.gateway.client-id` | Default client ID for operations | - |
| `payment.gateway.connection-timeout` | Connection timeout in milliseconds | `30000` |
| `payment.gateway.read-timeout` | Read timeout in milliseconds | `30000` |
| `payment.gateway.test-mode` | Enable test mode | `false` |

## Error Handling

The SDK provides comprehensive error handling with specific exceptions:

- **PaymentException**: General payment operation errors
- **TenantException**: Tenant-specific errors
- **AuthenticationException**: Authentication and authorization errors

All exceptions include detailed error messages and the original HTTP response for debugging.

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- 📖 [Documentation](https://github.com/yourusername/payment-gateway-sdk/wiki)
- 🐛 [Issue Tracker](https://github.com/yourusername/payment-gateway-sdk/issues)
- 💬 [Discussions](https://github.com/yourusername/payment-gateway-sdk/discussions)

## Requirements

- Java 17 or higher
- Spring Boot 3.2.0 or higher
- Maven 3.6 or higher
