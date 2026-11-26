package cl.veterinaria.sanmarcos.repository;

import cl.veterinaria.sanmarcos.model.RefreshToken;
import cl.veterinaria.sanmarcos.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select refresh from RefreshToken refresh where refresh.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);
    void deleteByUsuario(Usuario usuario);
}
