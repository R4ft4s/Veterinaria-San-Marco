package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.dto.AuthResponse;
import cl.veterinaria.sanmarcos.dto.LoginRequest;
import cl.veterinaria.sanmarcos.dto.RefreshRequest;
import cl.veterinaria.sanmarcos.dto.UsuarioDto;
import cl.veterinaria.sanmarcos.dto.UsuarioRequest;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.service.AuthService;
import cl.veterinaria.sanmarcos.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final UsuarioService usuarios;
    public AuthController(AuthService auth, UsuarioService usuarios) { this.auth = auth; this.usuarios = usuarios; }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @PostMapping("/registro") @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@Valid @RequestBody UsuarioRequest request) { return usuarios.registrar(request); }
    @GetMapping("/me") public UsuarioDto me(@AuthenticationPrincipal Usuario actual) { return usuarios.actual(actual); }
    @PutMapping("/perfil")     public UsuarioDto perfil(@Valid @RequestBody UsuarioRequest request, @AuthenticationPrincipal Usuario actual) {
        return usuarios.actualizarPerfil(request, actual);
    }
    @PostMapping("/refresh") public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) { return auth.refresh(request); }
    @PostMapping("/logout") @PreAuthorize("isAuthenticated()")
    public void logout(@AuthenticationPrincipal Usuario actual) { auth.logout(actual); }
}
