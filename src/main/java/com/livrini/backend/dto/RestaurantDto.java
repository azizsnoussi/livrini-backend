package com.livrini.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDto {
    private UUID id;
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private Double lat;
    private Double lng;
    private Boolean isOpen;
    private BigDecimal deliveryFee;
    private Integer etaMinutes;
}
