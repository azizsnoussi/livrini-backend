package com.livrini.backend.controller;

import com.livrini.backend.dto.LocationUpdateRequest;
import com.livrini.backend.dto.OrderDto;
import com.livrini.backend.dto.UpdateOrderStatusRequest;
import com.livrini.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/courier")
@RequiredArgsConstructor
public class CourierController {

    private final OrderService orderService;

    @PostMapping("/online")
    public ResponseEntity<Void> toggleOnline(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody Map<String, Boolean> body) {
        boolean isOnline = body.getOrDefault("isOnline", false);
        orderService.updateCourierOnlineStatus(userDetails.getUsername(), isOnline);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody LocationUpdateRequest request) {
        orderService.updateCourierLocation(userDetails.getUsername(), request.getLat(), request.getLng(),
                request.getOrderId(), request.getTimestamp());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available-orders")
    public ResponseEntity<List<OrderDto>> getAvailableOrders() {
        return ResponseEntity.ok(orderService.getAvailableOrdersForCourier());
    }

    @PostMapping("/orders/{id}/accept")
    public ResponseEntity<OrderDto> acceptOrder(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.acceptOrderAsCourier(id, userDetails.getUsername()));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable UUID id,
                                                     @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatusByCourier(id, userDetails.getUsername(), request.getStatus()));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getCourierOrders(userDetails.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<com.livrini.backend.dto.StatsDto> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getCourierStats(userDetails.getUsername()));
    }
}
