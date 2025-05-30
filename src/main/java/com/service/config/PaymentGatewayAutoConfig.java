package com.service.config;


import com.service.PaymentGatewayClient;
import com.service.exception.PaymentErrorHandler;
import com.service.exception.TenantErrorHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(PaymentClientProperties.class)
public class PaymentGatewayAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate paymentRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new PaymentErrorHandler());
        restTemplate.setErrorHandler(new TenantErrorHandler());
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayClient paymentGatewayClient(
            RestTemplate paymentRestTemplate,
            PaymentClientProperties properties) {
        return new PaymentGatewayClient(paymentRestTemplate, properties);
    }
}
