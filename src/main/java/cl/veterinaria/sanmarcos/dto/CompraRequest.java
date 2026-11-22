package cl.veterinaria.sanmarcos.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CompraRequest(@NotEmpty @Valid List<ItemRequest> items, String direccionEntrega, String regionEntrega,
                            String tipoEntrega, String metodoPago, String numeroTarjeta) {
    public record ItemRequest(@NotBlank String productoId, @Positive Integer cantidad) {}
}
