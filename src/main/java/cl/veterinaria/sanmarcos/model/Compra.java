package cl.veterinaria.sanmarcos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compra {
    @Id
    @Column(name = "id_orden", length = 50)
    private String idOrden;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(nullable = false)
    private Instant fecha;
    @Column(name = "direccion_entrega", nullable = false, length = 200)
    private String direccionEntrega;
    @Column(name = "region_entrega", nullable = false, length = 60)
    private String regionEntrega;
    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private String tipoEntrega;
    @Column(name = "metodo_pago", nullable = false, length = 24)
    private String metodoPago;
    @Column(nullable = false, length = 24)
    private String estado = "confirmado";
    @Column(nullable = false)
    private int total;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "detalle_compra", joinColumns = @JoinColumn(name = "id_orden"))
    private List<LineaCompra> items = new ArrayList<>();

    protected Compra() {}
    public Compra(String idOrden, Usuario usuario, Instant fecha, String direccionEntrega, String regionEntrega,
                  String tipoEntrega, String metodoPago, int total, List<LineaCompra> items) {
        this.idOrden = idOrden;
        this.usuario = usuario;
        this.fecha = fecha;
        this.direccionEntrega = direccionEntrega;
        this.regionEntrega = regionEntrega;
        this.tipoEntrega = tipoEntrega;
        this.metodoPago = metodoPago;
        this.total = total;
        this.items = new ArrayList<>(items);
    }
    public String getIdOrden() { return idOrden; }
    public String getUsuarioId() { return usuario == null ? null : usuario.getId(); }
    public Instant getFecha() { return fecha; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public String getRegionEntrega() { return regionEntrega; }
    public String getTipoEntrega() { return tipoEntrega; }
    public String getMetodoPago() { return metodoPago; }
    public String getEstado() { return estado; }
    public int getTotal() { return total; }
    public List<LineaCompra> getItems() { return items; }
    public void setEstado(String estado) { this.estado = estado; }

    @Embeddable
    public static class LineaCompra {
        @Column(name = "producto_id", nullable = false, length = 40)
        private String id;
        @Column(nullable = false, length = 150)
        private String nombre;
        @Column(nullable = false)
        private int cantidad;
        @Column(name = "precio_unitario", nullable = false)
        private int precioUnitario;
        @Column(nullable = false)
        private int subtotal;
        protected LineaCompra() {}
        public LineaCompra(String id, String nombre, int cantidad, int precioUnitario, int subtotal) {
            this.id = id;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
        public int getPrecioUnitario() { return precioUnitario; }
        public int getSubtotal() { return subtotal; }
    }
}
