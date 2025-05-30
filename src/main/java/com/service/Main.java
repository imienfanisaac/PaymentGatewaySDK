package com.service;

import com.service.account.model.AccountDto;
import com.service.bank.model.BankDto;
import com.service.bank.model.BankRequestDto;
import com.service.config.PaymentClientProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Main class for testing the SDK functionality.
 * This class provides examples of using the SDK.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner run(PaymentGatewayClient client) {
        return args -> {
            System.out.println("=== Testing Payment Gateway SDK ===");

            // Example: Get all banks
            try {
                System.out.println("\n=== Getting all banks ===");
                List<BankDto> banks = client.getBankClient().getBanks();
                banks.forEach(bank -> System.out.println("Bank: " + bank));
            } catch (Exception e) {
                System.err.println("Error getting banks: " + e.getMessage());
            }

            // Your other tests...
            System.out.println("\n=== SDK Test Complete ===");
        };
    }
}
