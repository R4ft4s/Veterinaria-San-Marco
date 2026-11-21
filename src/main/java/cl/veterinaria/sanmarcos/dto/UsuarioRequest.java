package cl.veterinaria.sanmarcos.dto;

import cl.veterinaria.sanmarcos.model.Rol;

public record UsuarioRequest(String nombre, String apellido, String fechaNacimiento, String correo, String password,
                             String direccion, String region, String genero, Rol role) {}
