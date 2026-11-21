package cl.veterinaria.sanmarcos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @Column(length = 40)
    private String id;
    @Column(nullable = false, length = 80)
    private String categoria;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(name = "principio_activo", nullable = false, length = 150)
    private String principioActivo;
    @Column(nullable = false, length = 100)
    private String presentacion;
    @Column(nullable = false, length = 80)
    private String especie;
    @Column(nullable = false)
    private int stock;
    @Column(nullable = false)
    private int precio;
    @Column(name = "precio_oferta")
    private Integer precioOferta;
    @Column(nullable = false, length = 12)
    private String icono;

    protected Producto() {}
    public Producto(String id, String categoria, String nombre, String principioActivo, String presentacion,
                    String especie, int stock, int precio, Integer precioOferta, String icono) {
        this.id = id;
        this.categoria = categoria;
        this.nombre = nombre;
        this.principioActivo = principioActivo;
        this.presentacion = presentacion;
        this.especie = especie;
        this.stock = stock;
        this.precio = precio;
        this.precioOferta = precioOferta;
        this.icono = icono;
    }
    public String getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getNombre() { return nombre; }
    public String getPrincipioActivo() { return principioActivo; }
    public String getPresentacion() { return presentacion; }
    public String getEspecie() { return especie; }
    public int getStock() { return stock; }
    public int getPrecio() { return precio; }
    public Integer getPrecioOferta() { return precioOferta; }
    public String getIcono() { return icono; }
    public void actualizar(String categoria, String nombre, String principioActivo, String presentacion,
                           String especie, int stock, int precio, Integer precioOferta, String icono) {
        this.categoria = categoria;
        this.nombre = nombre;
        this.principioActivo = principioActivo;
        this.presentacion = presentacion;
        this.especie = especie;
        this.stock = stock;
        this.precio = precio;
        this.precioOferta = precioOferta;
        this.icono = icono;
    }
    public void setStock(int stock) { this.stock = stock; }
}
