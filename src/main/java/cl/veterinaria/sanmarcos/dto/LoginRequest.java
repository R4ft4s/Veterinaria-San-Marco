package cl.veterinaria.sanmarcos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String correo, @NotBlank String password) {}
