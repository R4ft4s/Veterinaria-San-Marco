package cl.veterinaria.sanmarcos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @Column(length = 40)
    private String id;
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    protected Categoria() {}
    public Categoria(String id, String nombre) { this.id = id; this.nombre = nombre; }
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
