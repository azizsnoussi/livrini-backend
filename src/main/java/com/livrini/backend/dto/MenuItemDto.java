package com.livrini.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    private UUID id;
    private UUID restaurantId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    @NotBlank
    private String category;

    private List<String> composition;

    private Boolean isAvailable;
}
