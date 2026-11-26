package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.dto.UsuarioDto;
import cl.veterinaria.sanmarcos.dto.UsuarioRequest;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.service.UsuarioService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }
    @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public List<UsuarioDto> listar() { return service.listar(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest request, @AuthenticationPrincipal Usuario actual) { return service.crear(request, actual); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto actualizar(@PathVariable String id, @Valid @RequestBody UsuarioRequest request, @AuthenticationPrincipal Usuario actual) {
        return service.actualizar(id, request, actual);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable String id, @AuthenticationPrincipal Usuario actual) { service.eliminar(id, actual); }
    @PostMapping("/{id}/desbloquear") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto desbloquear(@PathVariable String id) { return service.desbloquear(id); }
    @PostMapping("/{id}/activar") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto activar(@PathVariable String id) { return service.activar(id); }
}
