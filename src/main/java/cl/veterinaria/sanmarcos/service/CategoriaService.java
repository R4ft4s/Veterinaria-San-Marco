package cl.veterinaria.sanmarcos.service;

import cl.veterinaria.sanmarcos.exception.ApiException;
import cl.veterinaria.sanmarcos.model.Categoria;
import cl.veterinaria.sanmarcos.repository.CategoriaRepository;
import cl.veterinaria.sanmarcos.repository.ProductoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }
    @Transactional(readOnly = true)
    public List<Categoria> listar() { return categorias.findAll(); }
    @Transactional
    public Categoria crear(String nombre) {
        String limpio = normalizar(nombre);
        if (categorias.existsByNombreIgnoreCase(limpio)) throw new ApiException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        return categorias.save(new Categoria("cat-" + UUID.randomUUID(), limpio));
    }
    @Transactional
    public Categoria actualizar(String id, String nombre) {
        Categoria categoria = buscar(id);
        String limpio = normalizar(nombre);
        if (categorias.existsByNombreIgnoreCase(limpio) && !categoria.getNombre().equalsIgnoreCase(limpio))
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        String anterior = categoria.getNombre();
        categoria.setNombre(limpio);
        productos.findByCategoriaIgnoreCase(anterior).forEach(producto -> producto.actualizar(limpio,
                producto.getNombre(), producto.getPrincipioActivo(), producto.getPresentacion(), producto.getEspecie(),
                producto.getStock(), producto.getPrecio(), producto.getPrecioOferta(), producto.getIcono()));
        return categorias.save(categoria);
    }
    @Transactional
    public void eliminar(String id) {
        Categoria categoria = buscar(id);
        if (!productos.findByCategoriaIgnoreCase(categoria.getNombre()).isEmpty())
            throw new ApiException(HttpStatus.CONFLICT, "No se puede eliminar una categoría que tiene productos asociados.");
        categorias.delete(categoria);
    }
    private Categoria buscar(String id) {
        return categorias.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La categoría no existe."));
    }
    private String normalizar(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre de categoría es obligatorio.");
        if (nombre.trim().length() > 80) throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre de categoría es demasiado largo.");
        return nombre.trim();
    }
}
