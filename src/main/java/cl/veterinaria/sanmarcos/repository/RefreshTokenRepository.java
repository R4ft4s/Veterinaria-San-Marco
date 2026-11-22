package cl.veterinaria.sanmarcos.repository;

import cl.veterinaria.sanmarcos.model.RefreshToken;
import cl.veterinaria.sanmarcos.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}
