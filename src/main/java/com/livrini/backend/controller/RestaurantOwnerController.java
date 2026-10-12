package com.livrini.backend.controller;

import com.livrini.backend.dto.MenuItemDto;
import com.livrini.backend.dto.OrderDto;
import com.livrini.backend.dto.RestaurantDto;
import com.livrini.backend.dto.UpdateOrderStatusRequest;
import com.livrini.backend.service.OrderService;
import com.livrini.backend.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantOwnerController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;

    @GetMapping("/me")
    public ResponseEntity<RestaurantDto> getMyRestaurant(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(restaurantService.getRestaurantByOwnerEmail(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<RestaurantDto> updateMyRestaurant(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestBody RestaurantDto dto) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(userDetails.getUsername(), dto));
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDto>> getMyMenu(@AuthenticationPrincipal UserDetails userDetails) {
        RestaurantDto r = restaurantService.getRestaurantByOwnerEmail(userDetails.getUsername());
        return ResponseEntity.ok(restaurantService.getRestaurantMenu(r.getId()));
    }

    @PostMapping("/menu")
    public ResponseEntity<MenuItemDto> addMenuItem(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody MenuItemDto dto) {
        return ResponseEntity.ok(restaurantService.addMenuItem(userDetails.getUsername(), dto));
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItemDto> updateMenuItem(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable UUID id,
                                                     @RequestBody MenuItemDto dto) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> deleteMenuItem(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable UUID id) {
        restaurantService.deleteMenuItem(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getRestaurantOrders(userDetails.getUsername()));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable UUID id,
                                                     @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatusByRestaurant(id, userDetails.getUsername(), request.getStatus()));
    }

    @GetMapping("/stats")
    public ResponseEntity<com.livrini.backend.dto.StatsDto> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getRestaurantStats(userDetails.getUsername()));
    }
}
