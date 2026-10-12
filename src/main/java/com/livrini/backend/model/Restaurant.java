package com.livrini.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String cuisine;

    private String address;

    private Double lat;

    private Double lng;

    @Builder.Default
    private Boolean isOpen = true;

    @Column(precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal deliveryFee = new BigDecimal("2.500");

    @Builder.Default
    private Integer etaMinutes = 30;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
