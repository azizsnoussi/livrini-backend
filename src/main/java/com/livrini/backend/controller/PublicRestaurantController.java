package com.livrini.backend.controller;

import com.livrini.backend.dto.MenuItemDto;
import com.livrini.backend.dto.RestaurantDto;
import com.livrini.backend.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class PublicRestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "6") Double radiusKm) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(lat, lng, radiusKm));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemDto>> getRestaurantMenu(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getRestaurantMenu(id));
    }
}
