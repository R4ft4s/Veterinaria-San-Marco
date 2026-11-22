package cl.veterinaria.sanmarcos.service;

import cl.veterinaria.sanmarcos.dto.UsuarioDto;
import cl.veterinaria.sanmarcos.dto.UsuarioRequest;
import cl.veterinaria.sanmarcos.exception.ApiException;
import cl.veterinaria.sanmarcos.model.Rol;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.repository.CompraRepository;
import cl.veterinaria.sanmarcos.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthService auth;
    private final CompraRepository compras;
    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, AuthService auth, CompraRepository compras) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.auth = auth;
        this.compras = compras;
    }
    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() { return usuarios.findAll().stream()
            .map(user -> UsuarioDto.de(user, compras.findByUsuario_IdOrderByFechaDesc(user.getId()))).toList(); }
    @Transactional(readOnly = true)
    public UsuarioDto actual(Usuario user) {
        return UsuarioDto.de(user, compras.findByUsuario_IdOrderByFechaDesc(user.getId()));
    }
    @Transactional
    public UsuarioDto registrar(UsuarioRequest request) { return auth.registrar(request); }
    @Transactional
    public UsuarioDto crear(UsuarioRequest request, Usuario actual) {
        if (actual.getRol() == Rol.VENDEDOR && request.role() != Rol.CLIENTE)
            throw new ApiException(HttpStatus.FORBIDDEN, "El vendedor solo puede crear cuentas de cliente.");
        if (request.role() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Debes seleccionar un rol.");
        auth.validarParaAdmin(request);
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByCorreoIgnoreCase(correo)) throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta registrada con ese correo.");
        Usuario user = new Usuario("usr-" + UUID.randomUUID(), request.nombre().trim(), request.apellido().trim(),
                request.fechaNacimiento(), correo, encoder.encode(request.password()), request.direccion(), request.region(), request.genero(), request.role());
        return UsuarioDto.de(usuarios.save(user));
    }
    @Transactional
    public UsuarioDto actualizar(String id, UsuarioRequest request, Usuario actual) {
        Usuario user = buscar(id);
        validarCorreo(request, id);
        if (actual.getId().equals(id) && request.role() != null && request.role() != actual.getRol())
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.");
        Rol rol = request.role() == null ? user.getRol() : request.role();
        user.actualizarDatos(request.nombre().trim(), request.apellido().trim(), request.fechaNacimiento(),
                request.correo().trim(), request.direccion(), request.region(), request.genero(), rol);
        if (request.password() != null && !request.password().isBlank()) {
            auth.validarPassword(request.password());
            user.setPasswordHash(encoder.encode(request.password()));
        }
        return actual(user);
    }
    @Transactional
    public UsuarioDto actualizarPerfil(UsuarioRequest request, Usuario actual) {
        validarCorreo(request, actual.getId());
        Usuario user = buscar(actual.getId());
        user.actualizarDatos(request.nombre().trim(), request.apellido().trim(), request.fechaNacimiento(),
                request.correo().trim(), request.direccion(), request.region(), request.genero(), user.getRol());
        return actual(user);
    }
    @Transactional
    public void eliminar(String id, Usuario actual) {
        if (actual.getId().equals(id)) throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta.");
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        usuarios.save(usuario);
    }
    @Transactional
    public UsuarioDto activar(String id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        return actual(usuario);
    }
    @Transactional
    public UsuarioDto desbloquear(String id) {
        Usuario user = buscar(id);
        user.setIntentosFallidos(0);
        return actual(user);
    }
    private Usuario buscar(String id) {
        return usuarios.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El usuario no existe."));
    }
    private void validarCorreo(UsuarioRequest request, String id) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()
                || request.apellido() == null || request.apellido().isBlank() || request.correo() == null || request.correo().isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nombre, apellido y correo son obligatorios.");
        if (!request.correo().trim().toLowerCase(Locale.ROOT).endsWith("@duoc.cl"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Usa un correo institucional válido con dominio @duoc.cl.");
        if (usuarios.existsByCorreoIgnoreCaseAndIdNot(request.correo().trim(), id))
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta registrada con ese correo.");
    }
}
