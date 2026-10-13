package com.livrini.backend.dto;

import com.livrini.backend.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private String clientPhone;

    private UUID restaurantId;
    private String restaurantName;
    private Double restaurantLat;
    private Double restaurantLng;

    private UUID courierId;
    private String courierName;
    private String courierPhone;
    private Double courierLat;
    private Double courierLng;

    private OrderStatus status;
    private String deliveryAddress;
    private String country;
    private String governorate;
    private String city;
    private String neighborhood;
    private String street;
    private String buildingNumber;
    private String apartment;
    private String postalCode;
    private Double lat;
    private Double lng;
    private String note;
    private String paymentMethod;

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;

    private List<OrderItemDto> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private UUID id;
        private String name;
        private BigDecimal unitPrice;
        private Integer quantity;
    }
}
