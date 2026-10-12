package com.livrini.backend.dto;

import com.livrini.backend.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
}
