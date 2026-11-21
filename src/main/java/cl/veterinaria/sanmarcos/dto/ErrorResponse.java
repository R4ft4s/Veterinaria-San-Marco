package cl.veterinaria.sanmarcos.dto;

import java.util.Map;

public record ErrorResponse(String error, Map<String, String> campos) {}
