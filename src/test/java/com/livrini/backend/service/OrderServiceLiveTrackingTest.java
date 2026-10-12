package com.livrini.backend.service;

import com.livrini.backend.dto.OrderDto;
import com.livrini.backend.model.*;
import com.livrini.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class OrderServiceLiveTrackingTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CustomerOrderRepository orderRepository;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private User client;
    private User courier;
    private User otherUser;
    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        client = userRepository.save(User.builder()
                .name("Client").email("client-lt@test.tn").phone("20000001")
                .passwordHash("x").role(Role.CLIENT).build());
        courier = userRepository.save(User.builder()
                .name("Courier").email("courier-lt@test.tn").phone("20000002")
                .passwordHash("x").role(Role.COURIER).build());
        otherUser = userRepository.save(User.builder()
                .name("Other").email("other-lt@test.tn").phone("20000003")
                .passwordHash("x").role(Role.CLIENT).build());
        User owner = userRepository.save(User.builder()
                .name("Owner").email("owner-lt@test.tn").phone("20000004")
                .passwordHash("x").role(Role.RESTAURANT).build());
        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .owner(owner).name("Resto").cuisine("Tunisian")
                .lat(36.8).lng(10.1).isOpen(true)
                .deliveryFee(new BigDecimal("2.500")).build());
        order = orderRepository.save(CustomerOrder.builder()
                .client(client).restaurant(restaurant).courier(courier)
                .status(OrderStatus.PICKED_UP)
                .subtotal(new BigDecimal("10.000"))
                .deliveryFee(new BigDecimal("2.500"))
                .total(new BigDecimal("12.500"))
                .lat(36.85).lng(10.18)
                .build());
    }

    @Test
    void locationUpdateBroadcastsToOrderTopicForAssignedCourier() {
        orderService.updateCourierLocation(courier.getEmail(), 36.81, 10.11, order.getId(),
                System.currentTimeMillis());

        User refreshed = userRepository.findByEmail(courier.getEmail()).orElseThrow();
        assertEquals(36.81, refreshed.getLat());
        assertEquals(10.11, refreshed.getLng());
        assertNotNull(refreshed.getLocationUpdatedAt());

        verify(messagingTemplate).convertAndSend(
                eq("/topic/orders/" + order.getId() + "/location"), any(Object.class));
    }

    @Test
    void locationUpdateDoesNotBroadcastWhenOrderNotAssignedToCourier() {
        orderService.updateCourierLocation(otherUser.getEmail(), 36.81, 10.11, order.getId(),
                System.currentTimeMillis());

        // otherUser is a CLIENT, not the assigned courier -> no broadcast.
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void locationUpdateRejectsInvalidCoordinates() {
        assertThrows(RuntimeException.class, () ->
                orderService.updateCourierLocation(courier.getEmail(), 200.0, 10.0,
                        order.getId(), System.currentTimeMillis()));
    }

    @Test
    void deliveredOrderDoesNotBroadcastLocation() {
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        orderService.updateCourierLocation(courier.getEmail(), 36.81, 10.11, order.getId(),
                System.currentTimeMillis());

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void getOrderByIdAllowsOwningClient() {
        OrderDto dto = orderService.getOrderById(order.getId(), client.getEmail());
        assertEquals(order.getId(), dto.getId());
    }

    @Test
    void getOrderByIdAllowsAssignedCourier() {
        OrderDto dto = orderService.getOrderById(order.getId(), courier.getEmail());
        assertEquals(order.getId(), dto.getId());
    }

    @Test
    void getOrderByIdDeniesUnrelatedUser() {
        assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(order.getId(), otherUser.getEmail()));
    }

    @Test
    void courierCanTransitionToOnTheWayAndDelivered() {
        OrderDto onWay = orderService.updateOrderStatusByCourier(order.getId(),
                courier.getEmail(), OrderStatus.ON_THE_WAY);
        assertEquals(OrderStatus.ON_THE_WAY, onWay.getStatus());

        OrderDto delivered = orderService.updateOrderStatusByCourier(order.getId(),
                courier.getEmail(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());
    }

    @Test
    void courierCannotSetInvalidStatus() {
        assertThrows(RuntimeException.class, () ->
                orderService.updateOrderStatusByCourier(order.getId(), courier.getEmail(),
                        OrderStatus.PREPARING));
    }

    @Test
    void unassignedCourierCannotUpdateStatus() {
        assertThrows(RuntimeException.class, () ->
                orderService.updateOrderStatusByCourier(order.getId(), otherUser.getEmail(),
                        OrderStatus.DELIVERED));
    }
}
