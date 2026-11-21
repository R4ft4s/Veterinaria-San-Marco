package cl.veterinaria.sanmarcos.repository;

import cl.veterinaria.sanmarcos.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") String id);
    List<Producto> findByNombreContainingIgnoreCaseOrPrincipioActivoContainingIgnoreCase(String nombre, String principioActivo);
    List<Producto> findByCategoriaIgnoreCase(String categoria);
}
