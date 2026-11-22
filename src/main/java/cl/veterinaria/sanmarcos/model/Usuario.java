package cl.veterinaria.sanmarcos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    @Id
    @Column(length = 40)
    private String id;
    @Column(nullable = false, length = 60)
    private String nombre;
    @Column(nullable = false, length = 60)
    private String apellido;
    @Column(name = "fecha_nacimiento", nullable = false, length = 20)
    private String fechaNacimiento = "";
    @Column(nullable = false, unique = true, length = 120)
    private String correo;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 200)
    private String direccion = "";
    @Column(nullable = false, length = 40)
    private String region = "";
    @Column(nullable = false, length = 30)
    private String genero = "";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.CLIENTE;
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;
    @Column(nullable = false)
    private boolean activo = true;

    protected Usuario() {}

    public Usuario(String id, String nombre, String apellido, String fechaNacimiento, String correo, String password,
                   String direccion, String region, String genero, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento == null ? "" : fechaNacimiento;
        this.correo = correo.toLowerCase();
        this.password = password;
        this.direccion = direccion == null ? "" : direccion;
        this.region = region == null ? "" : region;
        this.genero = genero == null ? "" : genero;
        this.rol = rol;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getCorreo() { return correo; }
    @Override public String getPassword() { return password; }
    public String getPasswordHash() { return password; }
    public String getDireccion() { return direccion; }
    public String getRegion() { return region; }
    public String getGenero() { return genero; }
    public Rol getRol() { return rol; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public boolean isBloqueado() { return intentosFallidos >= 3; }
    public boolean isActivo() { return activo; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setPasswordHash(String password) { this.password = password; }
    public void actualizarDatos(String nombre, String apellido, String fechaNacimiento, String correo,
                                String direccion, String region, String genero, Rol rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento == null ? "" : fechaNacimiento;
        this.correo = correo.toLowerCase();
        this.direccion = direccion == null ? "" : direccion;
        this.region = region == null ? "" : region;
        this.genero = genero == null ? "" : genero;
        this.rol = rol;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(rol); }
    @Override public String getUsername() { return correo; }
    @Override public boolean isAccountNonLocked() { return !isBloqueado(); }
}
