package com.monolith.orders.infra;

import com.monolith.common.exception.BusinessException;
import com.monolith.common.exception.EntityNotFoundException;
import com.monolith.notifications.api.NotificationService;
import com.monolith.notifications.api.dto.NotificationChannel;
import com.monolith.notifications.api.dto.SendNotificationRequest;
import com.monolith.orders.api.OrderService;
import com.monolith.orders.api.dto.*;
import com.monolith.payments.api.PaymentService;
import com.monolith.payments.api.dto.PaymentResponse;
import com.monolith.payments.api.dto.PaymentStatus;
import com.monolith.payments.api.dto.ProcessPaymentRequest;
import com.monolith.users.api.UserService;
import com.monolith.users.api.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Starting order creation for user: {}", request.getUserId());

        // 1. Verify User existence and validity via Users module facade
        UserResponse user = userService.getUserById(request.getUserId());
        if (!user.isActive()) {
            throw new BusinessException("User " + request.getUserId() + " is inactive and cannot place orders");
        }

        // 2. Build initial Order entity and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        OrderEntity orderEntity = OrderEntity.builder()
                .userId(user.getId())
                .status(OrderStatus.PENDING)
                .currency("USD")
                .shippingAddress(request.getShippingAddress() != null ? request.getShippingAddress() : user.getAddress())
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            OrderItemEntity itemEntity = OrderItemEntity.builder()
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .totalPrice(lineTotal)
                    .build();

            orderEntity.addItem(itemEntity);
        }

        orderEntity.setTotalAmount(totalAmount);
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        log.info("Saved order {} in PENDING state with total {}", savedOrder.getId(), totalAmount);

        // 3. Process payment via Payments module facade
        ProcessPaymentRequest paymentRequest = ProcessPaymentRequest.builder()
                .orderId(savedOrder.getId())
                .userId(user.getId())
                .amount(totalAmount)
                .currency(savedOrder.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .build();

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        // 4. Update order state based on payment result
        if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {
            savedOrder.setStatus(OrderStatus.PAID);
            savedOrder.setPaymentTransactionId(paymentResponse.getTransactionId());
            log.info("Order {} transitioned to PAID", savedOrder.getId());
        } else {
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Order {} payment failed: {}", savedOrder.getId(), paymentResponse.getFailureReason());
        }

        OrderEntity finalOrder = orderRepository.save(savedOrder);

        // 5. Notify user via Notifications module facade
        String subject = finalOrder.getStatus() == OrderStatus.PAID
                ? "Your order #" + finalOrder.getId() + " is confirmed!"
                : "Payment issue with your order #" + finalOrder.getId();

        String message = finalOrder.getStatus() == OrderStatus.PAID
                ? String.format("Hi %s, your order of $%s has been successfully paid and is being processed.",
                user.getFullName(), totalAmount)
                : String.format("Hi %s, payment for your order could not be completed (%s).",
                user.getFullName(), paymentResponse.getFailureReason());

        try {
            notificationService.sendNotification(SendNotificationRequest.builder()
                    .userId(user.getId())
                    .channel(NotificationChannel.EMAIL)
                    .recipient(user.getEmail())
                    .subject(subject)
                    .message(message)
                    .build());
        } catch (Exception e) {
            log.warn("Failed sending notification for order {}: {}", finalOrder.getId(), e.getMessage());
        }

        return mapToResponse(finalOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        OrderEntity entity = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(OrderEntity entity) {
        List<OrderItemResponse> itemResponses = entity.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .currency(entity.getCurrency())
                .shippingAddress(entity.getShippingAddress())
                .paymentTransactionId(entity.getPaymentTransactionId())
                .items(itemResponses)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
