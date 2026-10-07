package com.monolith.payments.infra;

import com.monolith.common.exception.BusinessException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class PaymentClient {

    private final RestClient restClient;
    private final String providerUrl;
    private final boolean mockMode;

    public PaymentClient(
            @Value("${payments.provider.url:https://api.payment-provider.example.com}") String providerUrl,
            @Value("${payments.provider.mock:true}") boolean mockMode) {
        this.providerUrl = providerUrl;
        this.mockMode = mockMode;
        this.restClient = RestClient.builder()
                .baseUrl(providerUrl)
                .build();
    }

    public PaymentProviderResult charge(UUID orderId, BigDecimal amount, String currency, String paymentMethod) {
        log.info("Sending charge request to Payment Provider for order: {}, amount: {} {}", orderId, amount, currency);

        if (mockMode) {
            // Simulated realistic external payment provider roundtrip
            try {
                Thread.sleep(150); // Simulate network latency to external provider
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Test failure simulation for special amount if needed
            if (amount.compareTo(BigDecimal.valueOf(999999)) >= 0) {
                log.warn("Payment rejected by provider: Insufficient funds or fraud check failed");
                return PaymentProviderResult.builder()
                        .successful(false)
                        .errorMessage("Card declined by external payment gateway")
                        .build();
            }

            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Payment approved by provider, transactionId: {}", transactionId);
            return PaymentProviderResult.builder()
                    .successful(true)
                    .transactionId(transactionId)
                    .build();
        }

        try {
            ProviderChargeRequest payload = ProviderChargeRequest.builder()
                    .orderId(orderId.toString())
                    .amount(amount)
                    .currency(currency)
                    .method(paymentMethod)
                    .build();

            return restClient.post()
                    .uri("/v1/charges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(PaymentProviderResult.class);
        } catch (Exception e) {
            log.error("Failed to connect to external payment provider: {}", e.getMessage());
            throw new BusinessException("External payment provider error: " + e.getMessage(), e);
        }
    }

    @Data
    @Builder
    public static class ProviderChargeRequest {
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private String method;
    }

    @Data
    @Builder
    public static class PaymentProviderResult {
        private boolean successful;
        private String transactionId;
        private String errorMessage;
    }
}
