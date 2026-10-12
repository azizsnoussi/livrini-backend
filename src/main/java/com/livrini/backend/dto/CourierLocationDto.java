package com.livrini.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierLocationDto {
    private UUID orderId;
    private UUID courierId;
    private String courierName;
    private Double lat;
    private Double lng;
    private Long timestamp;
}
