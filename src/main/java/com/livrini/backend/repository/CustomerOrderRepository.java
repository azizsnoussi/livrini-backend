package com.livrini.backend.repository;

import com.livrini.backend.model.CustomerOrder;
import com.livrini.backend.model.OrderStatus;
import com.livrini.backend.model.User;
import com.livrini.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByClientOrderByCreatedAtDesc(User client);
    List<CustomerOrder> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
    List<CustomerOrder> findByCourierOrderByCreatedAtDesc(User courier);
    List<CustomerOrder> findByStatus(OrderStatus status);
    List<CustomerOrder> findByCourierIsNullAndStatusIn(List<OrderStatus> statuses);
}
