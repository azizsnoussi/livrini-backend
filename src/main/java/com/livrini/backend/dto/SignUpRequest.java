package com.livrini.backend.dto;

import com.livrini.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String password;

    @NotNull
    private Role role;

    // Restaurant details if role == RESTAURANT
    private String restaurantName;
    private String cuisine;
    private String address;
    private Double lat;
    private Double lng;
}
