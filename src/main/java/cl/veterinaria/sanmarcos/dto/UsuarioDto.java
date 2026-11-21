package cl.veterinaria.sanmarcos.dto;

import cl.veterinaria.sanmarcos.model.Rol;
import cl.veterinaria.sanmarcos.model.Compra;
import cl.veterinaria.sanmarcos.model.Usuario;
import java.util.List;

public record UsuarioDto(String id, String nombre, String apellido, String fechaNacimiento, String correo,
                         String direccion, String region, String genero, Rol role,
                         List<CompraDto> compras, int intentosFallidos, boolean bloqueado) {
    public static UsuarioDto de(Usuario user) {
        return de(user, List.of());
    }
    public static UsuarioDto de(Usuario user, List<Compra> compras) {
        return new UsuarioDto(user.getId(), user.getNombre(), user.getApellido(), user.getFechaNacimiento(),
                user.getCorreo(), user.getDireccion(), user.getRegion(), user.getGenero(), user.getRol(),
                compras.stream().map(CompraDto::de).toList(), user.getIntentosFallidos(), user.isBloqueado());
    }
}
