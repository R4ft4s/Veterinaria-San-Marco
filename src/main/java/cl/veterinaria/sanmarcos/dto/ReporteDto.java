package cl.veterinaria.sanmarcos.dto;

import java.util.List;

public record ReporteDto(
        String periodo,
        long totalOperaciones,
        long montoTotal,
        long unidadesVendidas,
        List<ProductoSolicitadoDto> productosMasSolicitados
) {
    public record ProductoSolicitadoDto(String productoId, String nombre, int cantidad, long monto) {}
}
