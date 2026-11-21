package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.model.Categoria;
import cl.veterinaria.sanmarcos.service.CategoriaService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service) { this.service = service; }
    @GetMapping public List<Categoria> listar() { return service.listar(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Categoria crear(@RequestBody Map<String, String> body) { return service.crear(body.get("nombre")); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Categoria actualizar(@PathVariable String id, @RequestBody Map<String, String> body) { return service.actualizar(id, body.get("nombre")); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable String id) { service.eliminar(id); }
}
