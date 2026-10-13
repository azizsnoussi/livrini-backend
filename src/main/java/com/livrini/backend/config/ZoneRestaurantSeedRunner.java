package com.livrini.backend.config;

import com.livrini.backend.model.MenuItem;
import com.livrini.backend.model.Restaurant;
import com.livrini.backend.model.Role;
import com.livrini.backend.model.User;
import com.livrini.backend.repository.MenuItemRepository;
import com.livrini.backend.repository.RestaurantRepository;
import com.livrini.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ZoneRestaurantSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createIfMissing(
                "Mégrine Express", "zone.mégrine@livrio.tn", "+21620000103",
                "Fast Food Tunisien", "Mégrine, Ben Arous", 36.7695, 10.2317,
                new BigDecimal("2.000"), 25,
                List.of(new String[]{"Fricassé Mégrine", "Thon, œuf, pomme de terre et harissa", "4.500", "Sandwichs"},
                        new String[]{"Ojja Merguez", "Œufs, merguez et sauce tomate", "9.500", "Plats"}));
        createIfMissing(
                "El Aouina Kitchen", "zone.aouina@livrio.tn", "+21620000104",
                "Cuisine Tunisienne", "El Aouina, Tunis", 36.8580, 10.2510,
                new BigDecimal("2.500"), 30,
                List.of(new String[]{"Couscous Maison", "Couscous tunisien et légumes", "12.000", "Plats"},
                        new String[]{"Brik au Thon", "Brik croustillant au thon et œuf", "3.500", "Entrées"}));
        createIfMissing(
                "Cité Wahat Grill", "zone.wahat@livrio.tn", "+21620000105",
                "Grillades", "Cité Wahat, El Aouina", 36.8465, 10.2560,
                new BigDecimal("2.500"), 25,
                List.of(new String[]{"Grillade Mixte", "Escalope, merguez et salade tunisienne", "16.000", "Grillades"},
                        new String[]{"Pizza Wahat", "Sauce tomate, mozzarella et légumes", "11.000", "Pizzas"}));
    }

    private void createIfMissing(String name, String email, String phone, String cuisine,
                                 String address, double lat, double lng, BigDecimal fee,
                                 int eta, List<String[]> items) {
        if (restaurantRepository.findAll().stream().anyMatch(restaurant -> restaurant.getName().equalsIgnoreCase(name))) {
            return;
        }

        User owner = userRepository.save(User.builder()
                .name(name + " Owner")
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.RESTAURANT)
                .language("fr")
                .build());

        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .owner(owner)
                .name(name)
                .description("Restaurant de test pour la livraison par zone")
                .cuisine(cuisine)
                .address(address)
                .lat(lat)
                .lng(lng)
                .isOpen(true)
                .deliveryFee(fee)
                .etaMinutes(eta)
                .build());

        for (String[] item : items) {
            menuItemRepository.save(MenuItem.builder()
                    .restaurant(restaurant)
                    .name(item[0])
                    .description(item[1])
                    .price(new BigDecimal(item[2]))
                    .category(item[3])
                    .composition(new ArrayList<>())
                    .isAvailable(true)
                    .build());
        }
    }
}
