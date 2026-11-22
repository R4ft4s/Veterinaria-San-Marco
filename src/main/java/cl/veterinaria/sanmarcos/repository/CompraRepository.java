package cl.veterinaria.sanmarcos.repository;

import cl.veterinaria.sanmarcos.model.Compra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, String> {
    List<Compra> findByUsuario_IdOrderByFechaDesc(String usuarioId);
    List<Compra> findAllByOrderByFechaDesc();
}
