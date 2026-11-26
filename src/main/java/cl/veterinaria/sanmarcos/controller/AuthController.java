package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.dto.LoginRequest;
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
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    private final UsuarioService usuarios;
    public AuthController(AuthService auth, UsuarioService usuarios) { this.auth = auth; this.usuarios = usuarios; }
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) { return Map.of("token", csrfToken.getToken()); }
    @PostMapping("/login")
    public UsuarioDto login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) { return auth.login(request, response); }
    @PostMapping("/registro") @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@Valid @RequestBody UsuarioRequest request) { return usuarios.registrar(request); }
    @GetMapping("/me") public UsuarioDto me(@AuthenticationPrincipal Usuario actual) { return usuarios.actual(actual); }
    @PutMapping("/perfil")     public UsuarioDto perfil(@Valid @RequestBody UsuarioRequest request, @AuthenticationPrincipal Usuario actual) {
        return usuarios.actualizarPerfil(request, actual);
    }
    @PostMapping("/refresh")
    public UsuarioDto refresh(HttpServletRequest request, HttpServletResponse response) { return auth.refresh(request, response); }
    @PostMapping("/logout") @PreAuthorize("isAuthenticated()")
    public void logout(@AuthenticationPrincipal Usuario actual, HttpServletRequest request, HttpServletResponse response) {
        auth.logout(actual, request, response);
    }
}
