package com.livrini.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_item_components", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "component")
    @Builder.Default
    private List<String> composition = new ArrayList<>();

    @Builder.Default
    private Boolean isAvailable = true;
}
