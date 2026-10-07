package com.monolith.orders.infra;

import com.monolith.orders.api.OrderService;
import com.monolith.orders.api.dto.CreateOrderRequest;
import com.monolith.orders.api.dto.OrderResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final Timer orderCreationTimer;

    public OrderController(OrderService orderService, MeterRegistry meterRegistry) {
        this.orderService = orderService;
        this.orderCreationTimer = Timer.builder("orders.creation.latency")
                .description("Latency of POST /orders endpoint")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        long startTimeNano = System.nanoTime();
        try {
            OrderResponse response = orderService.createOrder(request);

            // Latency must be measured on OrderController before the return-statement (NFR requirement)
            long durationNano = System.nanoTime() - startTimeNano;
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNano);
            orderCreationTimer.record(durationNano, TimeUnit.NANOSECONDS);

            log.info("Order [{}] successfully processed. Measured endpoint latency: {} ms (SLO target: p95 < 800ms, p99 < 1300ms)",
                    response.getId(), durationMillis);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Response-Time-Millis", String.valueOf(durationMillis));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(response);
        } catch (Exception e) {
            long durationNano = System.nanoTime() - startTimeNano;
            orderCreationTimer.record(durationNano, TimeUnit.NANOSECONDS);
            log.warn("Failed order creation request after {} ms: {}",
                    TimeUnit.NANOSECONDS.toMillis(durationNano), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable UUID userId) {
        List<OrderResponse> response = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
