package cl.veterinaria.sanmarcos.dto;

import cl.veterinaria.sanmarcos.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(@NotBlank String nombre, @NotBlank String apellido, String fechaNacimiento,
                             @NotBlank @Email String correo, String password,
                             String direccion, String region, String genero, Rol role) {}
