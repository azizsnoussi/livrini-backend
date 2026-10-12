package com.livrini.backend.dto;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private Double lat;
    private Double lng;
    private java.util.UUID orderId;
    private Long timestamp;
}
