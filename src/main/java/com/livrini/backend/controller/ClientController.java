package com.livrini.backend.controller;

import com.livrini.backend.dto.CreateOrderRequest;
import com.livrini.backend.dto.OrderDto;
import com.livrini.backend.dto.StatsDto;
import com.livrini.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderDto> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(userDetails.getUsername(), request));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getClientOrders(userDetails.getUsername()));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDto> getOrderById(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id, userDetails.getUsername()));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userDetails.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getClientStats(userDetails.getUsername()));
    }
}
