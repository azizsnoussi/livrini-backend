package com.livrini.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    @NotNull
    private UUID restaurantId;

    @NotEmpty
    private List<OrderItemRequest> items;

    @NotBlank
    private String deliveryAddress;
    @NotBlank
    private String street;
    @NotBlank
    private String buildingNumber;
    @NotBlank
    private String city;
    private String country;
    private String governorate;
    private String neighborhood;
    private String apartment;
    private String postalCode;
    @AssertTrue
    private Boolean addressConfirmed;
    @NotNull
    private Double lat;
    @NotNull
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
