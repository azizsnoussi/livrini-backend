package com.livrini.backend.service;

import com.livrini.backend.dto.MenuItemDto;
import com.livrini.backend.dto.RestaurantDto;
import com.livrini.backend.model.MenuItem;
import com.livrini.backend.model.Restaurant;
import com.livrini.backend.repository.MenuItemRepository;
import com.livrini.backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public List<RestaurantDto> getAllRestaurants(Double lat, Double lng, Double radiusKm) {
        return restaurantRepository.findAll().stream()
                .filter(restaurant -> lat == null || lng == null || restaurant.getLat() == null || restaurant.getLng() == null
                        || distanceInKm(lat, lng, restaurant.getLat(), restaurant.getLng()) <= radiusKm)
                .map(this::mapToRestaurantDto)
                .collect(Collectors.toList());
    }

    private double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public RestaurantDto getRestaurantById(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        return mapToRestaurantDto(restaurant);
    }

    public List<MenuItemDto> getRestaurantMenu(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    public RestaurantDto getRestaurantByOwnerEmail(String email) {
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Restaurant not found for owner: " + email));
        return mapToRestaurantDto(restaurant);
    }

    @Transactional
    public RestaurantDto updateRestaurant(String ownerEmail, RestaurantDto dto) {
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getEmail().equalsIgnoreCase(ownerEmail))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Restaurant not found for owner: " + ownerEmail));

        if (dto.getName() != null) restaurant.setName(dto.getName());
        if (dto.getDescription() != null) restaurant.setDescription(dto.getDescription());
        if (dto.getCuisine() != null) restaurant.setCuisine(dto.getCuisine());
        if (dto.getAddress() != null) restaurant.setAddress(dto.getAddress());
        if (dto.getLat() != null) restaurant.setLat(dto.getLat());
        if (dto.getLng() != null) restaurant.setLng(dto.getLng());
        if (dto.getIsOpen() != null) restaurant.setIsOpen(dto.getIsOpen());
        if (dto.getDeliveryFee() != null) restaurant.setDeliveryFee(dto.getDeliveryFee());
        if (dto.getEtaMinutes() != null) restaurant.setEtaMinutes(dto.getEtaMinutes());

        return mapToRestaurantDto(restaurantRepository.save(restaurant));
    }

    @Transactional
    public MenuItemDto addMenuItem(String ownerEmail, MenuItemDto dto) {
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getEmail().equalsIgnoreCase(ownerEmail))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .composition(dto.getComposition() != null ? dto.getComposition() : new java.util.ArrayList<>())
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .build();

        return mapToMenuItemDto(menuItemRepository.save(menuItem));
    }

    @Transactional
    public MenuItemDto updateMenuItem(String ownerEmail, UUID itemId, MenuItemDto dto) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!menuItem.getRestaurant().getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Unauthorized to modify this menu item");
        }

        if (dto.getName() != null) menuItem.setName(dto.getName());
        if (dto.getDescription() != null) menuItem.setDescription(dto.getDescription());
        if (dto.getPrice() != null) menuItem.setPrice(dto.getPrice());
        if (dto.getCategory() != null) menuItem.setCategory(dto.getCategory());
        if (dto.getComposition() != null) menuItem.setComposition(dto.getComposition());
        if (dto.getIsAvailable() != null) menuItem.setIsAvailable(dto.getIsAvailable());

        return mapToMenuItemDto(menuItemRepository.save(menuItem));
    }

    @Transactional
    public void deleteMenuItem(String ownerEmail, UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!menuItem.getRestaurant().getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Unauthorized to delete this menu item");
        }

        menuItemRepository.delete(menuItem);
    }

    public RestaurantDto mapToRestaurantDto(Restaurant r) {
        return RestaurantDto.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .cuisine(r.getCuisine())
                .address(r.getAddress())
                .lat(r.getLat())
                .lng(r.getLng())
                .isOpen(r.getIsOpen())
                .deliveryFee(r.getDeliveryFee())
                .etaMinutes(r.getEtaMinutes())
                .build();
    }

    public MenuItemDto mapToMenuItemDto(MenuItem item) {
        return MenuItemDto.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurant().getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .composition(item.getComposition())
                .isAvailable(item.getIsAvailable())
                .build();
    }
}
