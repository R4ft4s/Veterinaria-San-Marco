package cl.veterinaria.sanmarcos.service;

import cl.veterinaria.sanmarcos.dto.AuthResponse;
import cl.veterinaria.sanmarcos.dto.LoginRequest;
import cl.veterinaria.sanmarcos.dto.UsuarioDto;
import cl.veterinaria.sanmarcos.exception.ApiException;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.repository.CompraRepository;
import cl.veterinaria.sanmarcos.repository.UsuarioRepository;
import cl.veterinaria.sanmarcos.security.JwtService;
import java.util.Locale;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final CompraRepository compras;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt, CompraRepository compras) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.compras = compras;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.correo() == null || request.password() == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes completar correo y contraseña.");
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        Usuario user = usuarios.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas."));
        if (user.isBloqueado()) throw new ApiException(HttpStatus.LOCKED, "La cuenta está bloqueada por superar 3 intentos fallidos.");
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            user.setIntentosFallidos(user.getIntentosFallidos() + 1);
            usuarios.save(user);
            int restantes = Math.max(0, 3 - user.getIntentosFallidos());
            throw new ApiException(restantes == 0 ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED,
                    restantes == 0 ? "Cuenta bloqueada: se alcanzaron 3 intentos fallidos."
                            : "Credenciales incorrectas. Te quedan " + restantes + " intentos.");
        }
        if (user.getIntentosFallidos() > 0) {
            user.setIntentosFallidos(0);
            usuarios.save(user);
        }
        return new AuthResponse(jwt.crear(user), UsuarioDto.de(user, compras.findByUsuarioIdOrderByFechaDesc(user.getId())));
    }

    @Transactional
    public UsuarioDto registrar(cl.veterinaria.sanmarcos.dto.UsuarioRequest request) {
        validarDatos(request, true);
        validarRegistro(request);
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByCorreoIgnoreCase(correo)) throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta registrada con ese correo.");
        Usuario user = new Usuario("usr-" + java.util.UUID.randomUUID(), request.nombre().trim(), request.apellido().trim(),
                request.fechaNacimiento(), correo, encoder.encode(request.password()), request.direccion(), request.region(),
                request.genero(), cl.veterinaria.sanmarcos.model.Rol.CLIENTE);
        return UsuarioDto.de(usuarios.save(user));
    }

    public void validarParaAdmin(cl.veterinaria.sanmarcos.dto.UsuarioRequest request) {
        validarDatos(request, true);
    }

    public void validarPassword(String password) {
        if (password == null || password.length() < 8
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isDigit))
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener 8 caracteres, una mayúscula y un número.");
        if (password.length() > 72) throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña no puede superar los 72 caracteres.");
    }

    private void validarDatos(cl.veterinaria.sanmarcos.dto.UsuarioRequest request, boolean conPassword) {
        if (request == null || vacio(request.nombre()) || vacio(request.apellido()) || vacio(request.correo()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nombre, apellido y correo son obligatorios.");
        if (!request.correo().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ingresa un correo electrónico válido.");
        if (!request.correo().trim().toLowerCase(Locale.ROOT).endsWith("@duoc.cl"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Usa un correo institucional válido con dominio @duoc.cl.");
        if (request.nombre().trim().length() > 60 || request.apellido().trim().length() > 60)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nombre y apellido no pueden superar los 60 caracteres.");
        if (conPassword) {
            validarPassword(request.password());
        }
    }

    private void validarRegistro(cl.veterinaria.sanmarcos.dto.UsuarioRequest request) {
        if (vacio(request.fechaNacimiento()) || vacio(request.direccion()) || vacio(request.region()) || vacio(request.genero()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fecha de nacimiento, dirección, región y género son obligatorios.");
        try {
            LocalDate nacimiento = LocalDate.parse(request.fechaNacimiento());
            int edad = Period.between(nacimiento, LocalDate.now()).getYears();
            if (nacimiento.isAfter(LocalDate.now()) || edad < 14)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Debes tener al menos 14 años para registrarte.");
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no es válida.");
        }
        if (request.direccion().trim().length() > 200 || request.region().trim().length() > 40 || request.genero().trim().length() > 30)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uno o más datos del perfil exceden el largo permitido.");
    }
    private boolean vacio(String value) { return value == null || value.isBlank(); }
}
