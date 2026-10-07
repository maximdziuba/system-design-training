package com.monolith.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monolith.notifications.api.NotificationService;
import com.monolith.notifications.api.dto.NotificationResponse;
import com.monolith.orders.api.dto.*;
import com.monolith.payments.api.PaymentService;
import com.monolith.payments.api.dto.PaymentResponse;
import com.monolith.payments.api.dto.PaymentStatus;
import com.monolith.users.api.UserService;
import com.monolith.users.api.dto.CreateUserRequest;
import com.monolith.users.api.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("Should successfully create order, process payment, send notification, and measure latency")
    void testCreateOrderFlow() throws Exception {
        // 1. Create User via Users module public API
        UserResponse user = userService.createUser(CreateUserRequest.builder()
                .email("john.doe@example.com")
                .fullName("John Doe")
                .phone("+380501234567")
                .address("123 Main Street, Kyiv")
                .build());
        assertThat(user.getId()).isNotNull();

        // 2. Prepare Order request
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(user.getId())
                .shippingAddress("123 Main Street, Kyiv")
                .paymentMethod("CREDIT_CARD")
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId("PROD-001")
                                .productName("Mechanical Keyboard")
                                .quantity(1)
                                .unitPrice(new BigDecimal("120.00"))
                                .build(),
                        OrderItemRequest.builder()
                                .productId("PROD-002")
                                .productName("Ergonomic Mouse")
                                .quantity(2)
                                .unitPrice(new BigDecimal("40.00"))
                                .build()
                ))
                .build();

        // 3. Post to /api/orders
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Response-Time-Millis"))
                .andReturn();

        OrderResponse orderResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderResponse.class);

        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(orderResponse.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(orderResponse.getPaymentTransactionId()).isNotBlank();
        assertThat(orderResponse.getItems()).hasSize(2);

        // 4. Verify Payment record via Payments public API
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderResponse.getId());
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getTransactionId()).isEqualTo(orderResponse.getPaymentTransactionId());

        // 5. Verify Notification was sent via Notifications public API
        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(user.getId());
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getRecipient()).isEqualTo(user.getEmail());

        // 6. Verify GET order by ID
        mockMvc.perform(get("/api/orders/" + orderResponse.getId()))
                .andExpect(status().isOk());
    }
}
