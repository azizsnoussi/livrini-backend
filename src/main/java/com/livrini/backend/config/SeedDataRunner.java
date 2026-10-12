package com.livrini.backend.config;

import com.livrini.backend.model.*;
import com.livrini.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Already seeded
        }

        // 1. Create Demo Client
        User client = User.builder()
                .name("Client Tunis")
                .email("client@livrio.tn")
                .phone("+21620000001")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.CLIENT)
                .language("fr")
                .lat(36.8383)
                .lng(10.1566) // Tunis Menzah 6
                .build();
        userRepository.save(client);

        // 2. Create Demo Courier
        User courier = User.builder()
                .name("Livreur Mohamed")
                .email("courier@livrio.tn")
                .phone("+21620000002")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.COURIER)
                .language("fr")
                .isOnline(true)
                .lat(36.8350)
                .lng(10.1550)
                .build();
        userRepository.save(courier);

        // 3. Create Restaurant Owners and Restaurants
        createRestaurantWithOwner("Weld El Hajj", "resto1@livrio.tn", "+21620000003",
                "Tunisien", "Chez Weld El Hajj", "Le spécialiste du Lablabi et Brik à Tunis",
                "Avenue Habib Bourguiba, Tunis", 36.8065, 10.1815, new BigDecimal("2.500"), 30,
                new String[][]{
                        {"Lablabi Spécial", "Pois chiches chauds, pain rassasié, huile d'olive, cumin, oeuf", "6.500", "Plats", "Pois chiches|Pain|Harissa|Cumin|Huile d'olive|Oeuf|Câpres"},
                        {"Brik au Thon", "Feuille de brick croustillante, oeuf, thon, persil, câpres", "3.200", "Entrées", "Feuille de brick|Thon|Oeuf|Persil|Câpres|Harissa"},
                        {"Ojja Merguez", "Sauce tomate pimentée, oeufs pochés, merguez artisanales", "12.000", "Plats", "Merguez|Oeufs|Sauce tomate|Poivron|Ail|Harissa"},
                        {"Kafteji", "Légumes frits, oeufs, harissa", "7.500", "Plats", "Pommes de terre|Poivron|Courgette|Oeufs|Tomate|Harissa"}
                });

        createRestaurantWithOwner("Dar El Brik", "resto2@livrio.tn", "+21620000004",
                "Tunisien", "Dar El Brik", "Chapatis, Fricassés et Makloub chauds faits minute",
                "Ennasr 2, Ariana", 36.8550, 10.1650, new BigDecimal("2.000"), 20,
                new String[][]{
                        {"Fricassé", "Petit pain frit, thon, pomme de terre, harissa, olives", "1.800", "Entrées", "Pain frit|Thon|Pomme de terre|Harissa|Olives|Oeuf|Câpres"},
                        {"Chapati Mahdia", "Galette fine garnie de thon, fromage, harissa", "5.000", "Plats", "Chapati|Thon|Fromage|Harissa|Mayonnaise|Salade|Frites"},
                        {"Makloub Escalope", "Sandwich tunisien au pain rond cuit au four", "6.500", "Plats", "Makloub|Escalope|Fromage|Harissa|Mayonnaise|Salade|Frites"}
                });

        createRestaurantWithOwner("Pizza du Lac", "resto3@livrio.tn", "+21620000005",
                "Pizza & Fast Food", "Pizza du Lac", "Pizzas au feu de bois et Calzones savoureuses",
                "Les Berges du Lac, Tunis", 36.8320, 10.2350, new BigDecimal("3.000"), 35,
                new String[][]{
                        {"Pizza Reine", "Sauce tomate, mozzarella, jambon, champignons", "14.500", "Pizzas", "Sauce tomate|Mozzarella|Jambon|Champignons|Olives|Origan"},
                        {"Pizza 4 Fromages", "Mozzarella, gorgonzola, parmesan, emmental", "16.000", "Pizzas", "Mozzarella|Gorgonzola|Parmesan|Emmental|Sauce tomate"},
                        {"Tiramisu Maison", "Dessert italien au café et mascarpone", "6.000", "Desserts", "Mascarpone|Café|Biscuit|Cacao"}
                });
    }

    private void createRestaurantWithOwner(String ownerName, String email, String phone,
                                           String cuisine, String restaurantName, String description,
                                           String address, double lat, double lng, BigDecimal fee, int eta,
                                           String[][] items) {
        User owner = User.builder()
                .name(ownerName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.RESTAURANT)
                .language("fr")
                .build();
        owner = userRepository.save(owner);

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(restaurantName)
                .description(description)
                .cuisine(cuisine)
                .address(address)
                .lat(lat)
                .lng(lng)
                .isOpen(true)
                .deliveryFee(fee)
                .etaMinutes(eta)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        for (String[] item : items) {
            List<String> composition = item.length > 4 && item[4] != null && !item[4].isEmpty()
                    ? Arrays.asList(item[4].split("\\|"))
                    : new java.util.ArrayList<>();
            MenuItem menuItem = MenuItem.builder()
                    .restaurant(restaurant)
                    .name(item[0])
                    .description(item[1])
                    .price(new BigDecimal(item[2]))
                    .category(item[3])
                    .composition(new java.util.ArrayList<>(composition))
                    .isAvailable(true)
                    .build();
            menuItemRepository.save(menuItem);
        }
    }
}
