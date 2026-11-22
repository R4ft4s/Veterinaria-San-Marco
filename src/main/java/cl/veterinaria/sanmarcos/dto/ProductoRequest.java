package cl.veterinaria.sanmarcos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoRequest(String id, @NotBlank String categoria, @NotBlank String nombre, @NotBlank String principioActivo,
                              @NotBlank String presentacion, @NotBlank String especie, @PositiveOrZero Integer stock, @PositiveOrZero Integer precio,
                              Integer precioOferta, String icono) {}
