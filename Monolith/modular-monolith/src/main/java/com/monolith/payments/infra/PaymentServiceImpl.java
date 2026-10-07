package com.monolith.payments.infra;

import com.monolith.common.exception.EntityNotFoundException;
import com.monolith.payments.api.PaymentService;
import com.monolith.payments.api.dto.PaymentResponse;
import com.monolith.payments.api.dto.PaymentStatus;
import com.monolith.payments.api.dto.ProcessPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentClient paymentClient;

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}, amount: {} {}",
                request.getOrderId(), request.getAmount(), request.getCurrency());

        PaymentEntity payment = PaymentEntity.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);

        PaymentClient.PaymentProviderResult providerResult = paymentClient.charge(
                request.getOrderId(),
                request.getAmount(),
                request.getCurrency(),
                payment.getPaymentMethod()
        );

        if (providerResult.isSuccessful()) {
            savedPayment.setStatus(PaymentStatus.SUCCESS);
            savedPayment.setTransactionId(providerResult.getTransactionId());
            log.info("Payment successfully processed for order: {}, txn: {}",
                    request.getOrderId(), providerResult.getTransactionId());
        } else {
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason(providerResult.getErrorMessage());
            log.warn("Payment failed for order: {}, reason: {}",
                    request.getOrderId(), providerResult.getErrorMessage());
        }

        PaymentEntity updated = paymentRepository.save(savedPayment);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        PaymentEntity entity = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        PaymentEntity entity = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order id: " + orderId));
        return mapToResponse(entity);
    }

    private PaymentResponse mapToResponse(PaymentEntity entity) {
        return PaymentResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .transactionId(entity.getTransactionId())
                .failureReason(entity.getFailureReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
