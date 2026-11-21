package cl.veterinaria.sanmarcos.service;

import cl.veterinaria.sanmarcos.dto.ProductoRequest;
import cl.veterinaria.sanmarcos.exception.ApiException;
import cl.veterinaria.sanmarcos.model.Producto;
import cl.veterinaria.sanmarcos.repository.ProductoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
    private final ProductoRepository productos;
    public ProductoService(ProductoRepository productos) { this.productos = productos; }
    @Transactional(readOnly = true)
    public List<Producto> listar(String texto, String categoria, Integer precioMax) {
        return productos.findAll().stream()
                .filter(p -> texto == null || texto.isBlank() || (p.getNombre() + " " + p.getPrincipioActivo()).toLowerCase().contains(texto.trim().toLowerCase()))
                .filter(p -> categoria == null || categoria.isBlank() || p.getCategoria().equalsIgnoreCase(categoria))
                .filter(p -> precioMax == null || (p.getPrecioOferta() != null && p.getPrecioOferta() < p.getPrecio() ? p.getPrecioOferta() : p.getPrecio()) <= precioMax)
                .toList();
    }
    @Transactional(readOnly = true)
    public Producto obtener(String id) { return buscar(id); }
    @Transactional
    public Producto crear(ProductoRequest r) {
        validar(r);
        String id = r.id() == null || r.id().isBlank() ? "prod-" + UUID.randomUUID() : r.id().trim();
        if (productos.existsById(id)) throw new ApiException(HttpStatus.CONFLICT, "Ya existe un producto con ese código.");
        return productos.save(new Producto(id, r.categoria().trim(), r.nombre().trim(), r.principioActivo().trim(),
                r.presentacion().trim(), r.especie().trim(), r.stock(), r.precio(), r.precioOferta(), r.icono().trim()));
    }
    @Transactional
    public Producto actualizar(String id, ProductoRequest r) {
        Producto actual = buscar(id);
        validar(r);
        actual.actualizar(r.categoria().trim(), r.nombre().trim(), r.principioActivo().trim(), r.presentacion().trim(),
                r.especie().trim(), r.stock(), r.precio(), r.precioOferta(), r.icono().trim());
        return actual;
    }
    @Transactional
    public Producto stock(String id, Integer stock) {
        if (stock == null || stock < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "El stock debe ser igual o mayor a cero.");
        Producto producto = buscar(id);
        producto.setStock(stock);
        return producto;
    }
    @Transactional
    public void eliminar(String id) { productos.delete(buscar(id)); }
    private Producto buscar(String id) { return productos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto no existe.")); }
    private void validar(ProductoRequest r) {
        if (r == null || vacio(r.categoria()) || vacio(r.nombre()) || vacio(r.principioActivo()) || vacio(r.presentacion()) || vacio(r.especie()) || vacio(r.icono()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Completa todos los datos del producto.");
        if (r.nombre().trim().length() > 150 || r.categoria().trim().length() > 80 || r.principioActivo().trim().length() > 150)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uno o más campos del producto exceden el largo permitido.");
        if (r.stock() == null || r.stock() < 0 || r.precio() == null || r.precio() < 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Precio y stock deben ser valores iguales o mayores a cero.");
        if (r.precioOferta() != null && (r.precioOferta() <= 0 || r.precioOferta() >= r.precio()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "El precio de oferta debe ser mayor a cero y menor al precio regular.");
    }
    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
}
