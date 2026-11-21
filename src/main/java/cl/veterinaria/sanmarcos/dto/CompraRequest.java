package cl.veterinaria.sanmarcos.dto;

import java.util.List;

public record CompraRequest(List<ItemRequest> items, String direccionEntrega, String regionEntrega,
                            String tipoEntrega, String metodoPago, String numeroTarjeta) {
    public record ItemRequest(String productoId, Integer cantidad) {}
}
