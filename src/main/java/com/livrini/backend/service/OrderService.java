package com.livrini.backend.service;

import com.livrini.backend.dto.*;
import com.livrini.backend.model.*;
import com.livrini.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public OrderDto createOrder(String clientEmail, CreateOrderRequest request) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getIsOpen()) {
            throw new RuntimeException("Restaurant is currently closed");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        CustomerOrder order = CustomerOrder.builder()
                .client(client)
                .restaurant(restaurant)
                .status(OrderStatus.PLACED)
                .deliveryAddress(request.getDeliveryAddress())
                .lat(request.getLat() != null ? request.getLat() : client.getLat())
                .lng(request.getLng() != null ? request.getLng() : client.getLng())
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH")
                .deliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : new BigDecimal("2.500"))
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemReq.getMenuItemId()));

                        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                                throw new RuntimeException("Menu item belongs to a different restaurant: " + menuItem.getName());
                        }

            if (!menuItem.getIsAvailable()) {
                throw new RuntimeException("Item is not available: " + menuItem.getName());
            }

            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .name(menuItem.getName())
                    .unitPrice(menuItem.getPrice())
                    .quantity(itemReq.getQuantity())
                    .build();

            orderItems.add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getDeliveryFee()));
        order.setItems(orderItems);

        return mapToOrderDto(orderRepository.save(order));
    }

    public List<OrderDto> getClientOrders(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return orderRepository.findByClientOrderByCreatedAtDesc(client).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(UUID id, String userEmail) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        boolean isClient = order.getClient() != null
                && order.getClient().getEmail().equalsIgnoreCase(userEmail);
        boolean isCourier = order.getCourier() != null
                && order.getCourier().getEmail().equalsIgnoreCase(userEmail);
        boolean isRestaurantOwner = order.getRestaurant() != null
                && order.getRestaurant().getOwner() != null
                && order.getRestaurant().getOwner().getEmail().equalsIgnoreCase(userEmail);
        if (!isClient && !isCourier && !isRestaurantOwner) {
            throw new RuntimeException("Unauthorized to view this order");
        }
        return mapToOrderDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(UUID id, String clientEmail) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getClient().getEmail().equalsIgnoreCase(clientEmail)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Order can only be cancelled while status is PLACED");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return mapToOrderDto(orderRepository.save(order));
    }

    // Restaurant Operations
    public List<OrderDto> getRestaurantOrders(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getEmail().equalsIgnoreCase(ownerEmail))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        return orderRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrderStatusByRestaurant(UUID orderId, String ownerEmail, OrderStatus newStatus) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getRestaurant().getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this order");
        }

        // Restaurant can set ACCEPTED, PREPARING, READY, or CANCELLED
        if (newStatus != OrderStatus.ACCEPTED && newStatus != OrderStatus.PREPARING &&
            newStatus != OrderStatus.READY && newStatus != OrderStatus.CANCELLED) {
            throw new RuntimeException("Invalid status transition for restaurant");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return mapToOrderDto(orderRepository.save(order));
    }

    // Courier Operations
    public List<OrderDto> getAvailableOrdersForCourier() {
        return orderRepository.findByCourierIsNullAndStatusIn(Arrays.asList(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY)).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto acceptOrderAsCourier(UUID orderId, String courierEmail) {
        User courier = userRepository.findByEmail(courierEmail)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getCourier() != null) {
            throw new RuntimeException("Order has already been accepted by another courier");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Order is no longer available");
        }

        order.setCourier(courier);
        order.setUpdatedAt(LocalDateTime.now());
        try {
            // @Version on CustomerOrder guarantees only the first concurrent
            // save succeeds; the second courier gets an optimistic lock failure.
            return mapToOrderDto(orderRepository.saveAndFlush(order));
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Order has already been accepted by another courier");
        }
    }

    // Statistics
    public StatsDto getCourierStats(String courierEmail) {
        User courier = userRepository.findByEmail(courierEmail)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        List<CustomerOrder> orders = orderRepository.findByCourierOrderByCreatedAtDesc(courier);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long deliveriesTotal = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long deliveriesToday = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getUpdatedAt().isAfter(startOfDay))
                .count();
        long active = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        // Courier earns the delivery fee per completed delivery.
        BigDecimal earningsTotal = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(CustomerOrder::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal earningsToday = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getUpdatedAt().isAfter(startOfDay))
                .map(CustomerOrder::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StatsDto.builder()
                .ordersTotal(orders.size())
                .activeOrders(active)
                .completedOrders(deliveriesTotal)
                .deliveriesTotal(deliveriesTotal)
                .deliveriesToday(deliveriesToday)
                .earningsTotal(earningsTotal)
                .earningsToday(earningsToday)
                .build();
    }

    public StatsDto getRestaurantStats(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getEmail().equalsIgnoreCase(ownerEmail))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        List<CustomerOrder> orders = orderRepository.findByRestaurantOrderByCreatedAtDesc(restaurant);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long ordersToday = orders.stream().filter(o -> o.getCreatedAt().isAfter(startOfDay)).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long active = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        // Restaurant revenue is the food subtotal of non-cancelled orders.
        BigDecimal revenueTotal = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(CustomerOrder::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal revenueToday = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getCreatedAt().isAfter(startOfDay))
                .map(CustomerOrder::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StatsDto.builder()
                .ordersTotal(orders.size())
                .ordersToday(ordersToday)
                .activeOrders(active)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .revenueTotal(revenueTotal)
                .revenueToday(revenueToday)
                .build();
    }

    public StatsDto getClientStats(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        List<CustomerOrder> orders = orderRepository.findByClientOrderByCreatedAtDesc(client);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long ordersToday = orders.stream().filter(o -> o.getCreatedAt().isAfter(startOfDay)).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long active = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        // Total spent by the client across non-cancelled orders.
        BigDecimal spentTotal = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(CustomerOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spentToday = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getCreatedAt().isAfter(startOfDay))
                .map(CustomerOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StatsDto.builder()
                .ordersTotal(orders.size())
                .ordersToday(ordersToday)
                .activeOrders(active)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .revenueTotal(spentTotal)
                .revenueToday(spentToday)
                .build();
    }

    @Transactional
    public OrderDto updateOrderStatusByCourier(UUID orderId, String courierEmail, OrderStatus newStatus) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getCourier() == null || !order.getCourier().getEmail().equalsIgnoreCase(courierEmail)) {
            throw new RuntimeException("Unauthorized: order not assigned to this courier");
        }

        if (newStatus != OrderStatus.PICKED_UP && newStatus != OrderStatus.ON_THE_WAY
                && newStatus != OrderStatus.DELIVERED) {
            throw new RuntimeException("Courier can only transition status to PICKED_UP, ON_THE_WAY or DELIVERED");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return mapToOrderDto(orderRepository.save(order));
    }

    public List<OrderDto> getCourierOrders(String courierEmail) {
        User courier = userRepository.findByEmail(courierEmail)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        return orderRepository.findByCourierOrderByCreatedAtDesc(courier).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateCourierLocation(String courierEmail, Double lat, Double lng, UUID orderId, Long timestamp) {
        if (lat == null || lng == null || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new RuntimeException("Invalid coordinates");
        }
        User courier = userRepository.findByEmail(courierEmail)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        courier.setLat(lat);
        courier.setLng(lng);
        courier.setLocationUpdatedAt(LocalDateTime.now());
        userRepository.save(courier);

        long ts = timestamp != null ? timestamp : System.currentTimeMillis();

        if (orderId != null) {
            CustomerOrder order = orderRepository.findById(orderId).orElse(null);
            // Only broadcast for an active delivery actually assigned to this courier.
            if (order != null
                    && order.getCourier() != null
                    && order.getCourier().getEmail().equalsIgnoreCase(courierEmail)
                    && order.getStatus() != OrderStatus.DELIVERED
                    && order.getStatus() != OrderStatus.CANCELLED) {
                CourierLocationDto dto = CourierLocationDto.builder()
                        .orderId(order.getId())
                        .courierId(courier.getId())
                        .courierName(courier.getName())
                        .lat(lat)
                        .lng(lng)
                        .timestamp(ts)
                        .build();
                messagingTemplate.convertAndSend("/topic/orders/" + order.getId() + "/location", dto);
            }
        }
    }

    @Transactional
    public void updateCourierOnlineStatus(String courierEmail, boolean isOnline) {
        User courier = userRepository.findByEmail(courierEmail)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        courier.setIsOnline(isOnline);
        userRepository.save(courier);
    }

    public OrderDto mapToOrderDto(CustomerOrder order) {
        List<OrderDto.OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> OrderDto.OrderItemDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        Double cLat = order.getCourier() != null ? order.getCourier().getLat() : null;
        Double cLng = order.getCourier() != null ? order.getCourier().getLng() : null;

        return OrderDto.builder()
                .id(order.getId())
                .clientId(order.getClient().getId())
                .clientName(order.getClient().getName())
                .clientPhone(order.getClient().getPhone())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .restaurantLat(order.getRestaurant().getLat())
                .restaurantLng(order.getRestaurant().getLng())
                .courierId(order.getCourier() != null ? order.getCourier().getId() : null)
                .courierName(order.getCourier() != null ? order.getCourier().getName() : null)
                .courierPhone(order.getCourier() != null ? order.getCourier().getPhone() : null)
                .courierLat(cLat)
                .courierLng(cLng)
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .lat(order.getLat())
                .lng(order.getLng())
                .note(order.getNote())
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .total(order.getTotal())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
