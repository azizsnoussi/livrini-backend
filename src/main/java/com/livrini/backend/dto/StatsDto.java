package com.livrini.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDto {
    private long ordersToday;
    private long ordersTotal;
    private long activeOrders;
    private long completedOrders;
    private long cancelledOrders;

    private BigDecimal revenueToday;
    private BigDecimal revenueTotal;

    private BigDecimal earningsToday;
    private BigDecimal earningsTotal;

    private long deliveriesToday;
    private long deliveriesTotal;
}
