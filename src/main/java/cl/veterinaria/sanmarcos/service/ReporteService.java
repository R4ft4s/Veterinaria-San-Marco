package cl.veterinaria.sanmarcos.service;

import cl.veterinaria.sanmarcos.dto.ReporteDto;
import cl.veterinaria.sanmarcos.exception.ApiException;
import cl.veterinaria.sanmarcos.model.Compra;
import cl.veterinaria.sanmarcos.repository.CompraRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {
    private final CompraRepository compras;

    public ReporteService(CompraRepository compras) {
        this.compras = compras;
    }

    @Transactional(readOnly = true)
    public ReporteDto resumen(String periodo) {
        String limpio = periodo == null ? "mes" : periodo.trim().toLowerCase(Locale.ROOT);
        Instant desde = switch (limpio) {
            case "semana" -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "mes" -> Instant.now().minus(30, ChronoUnit.DAYS);
            case "ano", "año" -> Instant.now().minus(365, ChronoUnit.DAYS);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "El periodo debe ser semana, mes o año.");
        };

        List<Compra> operaciones = compras.findAllByOrderByFechaDesc().stream()
                .filter(compra -> compra.getFecha().isAfter(desde))
                .filter(compra -> !"cancelado".equals(compra.getEstado()))
                .toList();

        long monto = operaciones.stream().mapToLong(Compra::getTotal).sum();
        long unidades = operaciones.stream()
                .flatMap(compra -> compra.getItems().stream())
                .mapToLong(item -> item.getCantidad()).sum();

        Map<String, Acumulado> productos = new HashMap<>();
        for (Compra compra : operaciones) {
            for (Compra.LineaCompra item : compra.getItems()) {
                Acumulado acumulado = productos.computeIfAbsent(item.getId(),
                        id -> new Acumulado(item.getNombre()));
                acumulado.cantidad += item.getCantidad();
                acumulado.monto += item.getSubtotal();
            }
        }

        List<ReporteDto.ProductoSolicitadoDto> masSolicitados = productos.entrySet().stream()
                .map(entry -> new ReporteDto.ProductoSolicitadoDto(entry.getKey(), entry.getValue().nombre,
                        entry.getValue().cantidad, entry.getValue().monto))
                .sorted(java.util.Comparator.comparingInt(ReporteDto.ProductoSolicitadoDto::cantidad).reversed())
                .limit(10)
                .toList();

        return new ReporteDto(limpio, operaciones.size(), monto, unidades, masSolicitados);
    }

    private static class Acumulado {
        private final String nombre;
        private int cantidad;
        private long monto;

        private Acumulado(String nombre) {
            this.nombre = nombre;
        }
    }
}
