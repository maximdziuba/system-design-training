package com.monolith.payments.api;

import com.monolith.payments.api.dto.PaymentResponse;
import com.monolith.payments.api.dto.ProcessPaymentRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse processPayment(ProcessPaymentRequest request);
    PaymentResponse getPaymentById(UUID id);
    PaymentResponse getPaymentByOrderId(UUID orderId);
}
