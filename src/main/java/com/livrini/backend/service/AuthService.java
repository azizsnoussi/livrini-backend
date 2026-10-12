package com.livrini.backend.service;

import com.livrini.backend.dto.*;
import com.livrini.backend.model.*;
import com.livrini.backend.repository.*;
import com.livrini.backend.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .language("fr")
                .isOnline(false)
                .build();

        user = userRepository.save(user);

        if (request.getRole() == Role.RESTAURANT) {
            String restName = request.getRestaurantName() != null ? request.getRestaurantName() : request.getName() + " Restaurant";
            String cuisine = request.getCuisine() != null ? request.getCuisine() : "Tunisien";

            Restaurant restaurant = Restaurant.builder()
                    .owner(user)
                    .name(restName)
                    .cuisine(cuisine)
                    .address(request.getAddress())
                    .lat(request.getLat() != null ? request.getLat() : 36.8065)
                    .lng(request.getLng() != null ? request.getLng() : 10.1815)
                    .isOpen(true)
                    .build();

            restaurantRepository.save(restaurant);
        }

        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .language(user.getLanguage())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .or(() -> userRepository.findByPhone(request.getEmail()))
                .orElseThrow(() -> new RuntimeException("Invalid email/phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email/phone or password");
        }

        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .language(user.getLanguage())
                .build();
    }

    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .language(user.getLanguage())
                .build();
    }
}
