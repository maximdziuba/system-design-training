package com.monolith.orders.api;

import com.monolith.orders.api.dto.CreateOrderRequest;
import com.monolith.orders.api.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(UUID id);
    List<OrderResponse> getOrdersByUserId(UUID userId);
}
