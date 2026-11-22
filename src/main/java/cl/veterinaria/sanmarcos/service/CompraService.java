package cl.veterinaria.sanmarcos.service;

import cl.veterinaria.sanmarcos.dto.CompraDto;
import cl.veterinaria.sanmarcos.dto.CompraRequest;
import cl.veterinaria.sanmarcos.exception.ApiException;
import cl.veterinaria.sanmarcos.model.Compra;
import cl.veterinaria.sanmarcos.model.Producto;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.repository.CompraRepository;
import cl.veterinaria.sanmarcos.repository.ProductoRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraService {
    private final CompraRepository compras;
    private final ProductoRepository productos;
    public CompraService(CompraRepository compras, ProductoRepository productos) {
        this.compras = compras;
        this.productos = productos;
    }

    @Transactional
    public CompraDto crear(CompraRequest request, Usuario usuario) {
        if (usuario == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para confirmar la compra.");
        validar(request);
        if ("tarjeta".equals(request.metodoPago()) && request.numeroTarjeta() != null
                && request.numeroTarjeta().replaceAll("\\s", "").endsWith("0002"))
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "El emisor rechazó el pago simulado. Puedes intentarlo de nuevo.");

        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (CompraRequest.ItemRequest item : request.items())
            cantidades.merge(item.productoId(), item.cantidad(), Math::addExact);

        List<Compra.LineaCompra> lineas = new ArrayList<>();
        List<Producto> productosActualizados = new ArrayList<>();
        long total = 0;
        for (var entrada : cantidades.entrySet()) {
            Producto producto = productos.findByIdForUpdate(entrada.getKey())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Uno de los productos ya no existe."));
            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad)
                throw new ApiException(HttpStatus.CONFLICT, "El stock de " + producto.getNombre() + " cambió y no alcanza para completar la compra.");
            int precio = producto.getPrecioOferta() != null && producto.getPrecioOferta() < producto.getPrecio()
                    ? producto.getPrecioOferta() : producto.getPrecio();
            int subtotal = Math.multiplyExact(precio, cantidad);
            total += subtotal;
            if (total > Integer.MAX_VALUE) throw new ApiException(HttpStatus.BAD_REQUEST, "El total de la compra supera el máximo permitido.");
            lineas.add(new Compra.LineaCompra(producto.getId(), producto.getNombre(), cantidad, precio, subtotal));
            producto.setStock(producto.getStock() - cantidad);
            productosActualizados.add(producto);
        }
        productos.saveAll(productosActualizados);
        String direccion = "retiro".equals(request.tipoEntrega()) ? "Retiro en clínica" : request.direccionEntrega().trim();
        String region = "retiro".equals(request.tipoEntrega()) ? "Libertador General Bernardo O'Higgins" : request.regionEntrega().trim();
        Compra compra = new Compra("ORD-" + UUID.randomUUID(), usuario, Instant.now(),
                direccion, region, request.tipoEntrega(), request.metodoPago(), (int) total, lineas);
        return CompraDto.de(compras.save(compra));
    }

    @Transactional(readOnly = true)
    public List<CompraDto> mias(Usuario usuario) {
        return compras.findByUsuario_IdOrderByFechaDesc(usuario.getId()).stream().map(CompraDto::de).toList();
    }
    @Transactional(readOnly = true)
    public List<CompraDto> todas() { return compras.findAllByOrderByFechaDesc().stream().map(CompraDto::de).toList(); }
    @Transactional
    public CompraDto actualizarEstado(String id, String estado) {
        if (estado == null || estado.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "El estado es obligatorio.");
        String limpio = estado.trim().toLowerCase(java.util.Locale.ROOT);
        if (!List.of("confirmado", "preparando", "despachado", "entregado", "cancelado").contains(limpio))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado de compra no válido.");
        Compra compra = compras.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La compra no existe."));
        compra.setEstado(limpio);
        return CompraDto.de(compras.save(compra));
    }

    private void validar(CompraRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito está vacío.");
        if (!"despacho".equals(request.tipoEntrega()) && !"retiro".equals(request.tipoEntrega()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "El tipo de entrega no es válido.");
        if (!"tarjeta".equals(request.metodoPago()) && !"transferencia".equals(request.metodoPago()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "El método de pago no es válido.");
        if ("despacho".equals(request.tipoEntrega()) && (vacio(request.direccionEntrega()) || vacio(request.regionEntrega())))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Indica la dirección y región de entrega.");
        for (var item : request.items()) {
            if (item == null || vacio(item.productoId()) || item.cantidad() == null || item.cantidad() < 1)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cada producto debe tener un código y una cantidad válida.");
        }
    }
    private boolean vacio(String value) { return value == null || value.isBlank(); }
}
