package com.livrini.backend.repository;

import com.livrini.backend.model.MenuItem;
import com.livrini.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);
    List<MenuItem> findByRestaurantId(UUID restaurantId);
}
