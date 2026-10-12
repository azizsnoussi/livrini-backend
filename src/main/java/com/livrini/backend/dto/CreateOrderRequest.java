package com.livrini.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    @NotNull
    private UUID restaurantId;

    @NotEmpty
    private List<OrderItemRequest> items;

    private String deliveryAddress;
    private Double lat;
    private Double lng;
    private String note;
    private String paymentMethod;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private UUID menuItemId;

        @NotNull
        private Integer quantity;
    }
}
