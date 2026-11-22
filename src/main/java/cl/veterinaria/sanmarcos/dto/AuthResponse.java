package cl.veterinaria.sanmarcos.dto;

public record AuthResponse(String token, String refreshToken, UsuarioDto usuario) {}
