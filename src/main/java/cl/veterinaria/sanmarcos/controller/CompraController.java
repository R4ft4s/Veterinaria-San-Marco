package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.dto.CompraDto;
import cl.veterinaria.sanmarcos.dto.CompraRequest;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.service.CompraService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compras")
public class CompraController {
    private final CompraService service;
    public CompraController(CompraService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CompraDto crear(@RequestBody CompraRequest request, @AuthenticationPrincipal Usuario usuario) {
        return service.crear(request, usuario);
    }
    @GetMapping("/mias") public List<CompraDto> mias(@AuthenticationPrincipal Usuario usuario) { return service.mias(usuario); }
    @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public List<CompraDto> todas() { return service.todas(); }
}
