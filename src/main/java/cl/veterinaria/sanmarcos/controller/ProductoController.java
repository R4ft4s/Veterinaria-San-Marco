package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.dto.ProductoRequest;
import cl.veterinaria.sanmarcos.model.Producto;
import cl.veterinaria.sanmarcos.service.ProductoService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }
    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String texto,
                                 @RequestParam(required = false) String categoria,
                                 @RequestParam(required = false) Integer precioMax) {
        return service.listar(texto, categoria, precioMax);
    }
    @GetMapping("/{id}") public Producto obtener(@PathVariable String id) { return service.obtener(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto crear(@Valid @RequestBody ProductoRequest request) { return service.crear(request); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto actualizar(@PathVariable String id, @Valid @RequestBody ProductoRequest request) { return service.actualizar(id, request); }
    @PatchMapping("/{id}/stock") @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public Producto stock(@PathVariable String id, @RequestBody Map<String, Integer> body) { return service.stock(id, body.get("stock")); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable String id) { service.eliminar(id); }
}
