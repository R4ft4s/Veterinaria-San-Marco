package cl.veterinaria.sanmarcos.dto;

import cl.veterinaria.sanmarcos.model.Compra;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record CompraDto(String idOrden, String fecha, String direccionEntrega, String regionEntrega,
                        String tipoEntrega, String metodoPago, String estado,
                        List<ItemDto> items, int total) {
    public static CompraDto de(Compra compra) {
        return new CompraDto(compra.getIdOrden(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                .withZone(java.time.ZoneId.of("America/Santiago")).format(compra.getFecha()),
                compra.getDireccionEntrega(), compra.getRegionEntrega(), compra.getTipoEntrega(),
                compra.getMetodoPago(), compra.getEstado(), compra.getItems().stream()
                .map(item -> new ItemDto(item.getId(), item.getNombre(), item.getCantidad(),
                        item.getPrecioUnitario(), item.getSubtotal())).toList(), compra.getTotal());
    }
    public record ItemDto(String id, String nombre, int cantidad, int precioUnitario, int subtotal) {}
}
