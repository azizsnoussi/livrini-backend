package com.livrini.backend.repository;

import com.livrini.backend.model.Restaurant;
import com.livrini.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findByOwner(User owner);
    Optional<Restaurant> findByOwnerId(UUID ownerId);
    List<Restaurant> findByIsOpenTrue();
}
