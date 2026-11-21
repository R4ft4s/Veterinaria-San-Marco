package cl.veterinaria.sanmarcos.config;

import cl.veterinaria.sanmarcos.model.Categoria;
import cl.veterinaria.sanmarcos.model.Producto;
import cl.veterinaria.sanmarcos.model.Rol;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.repository.CategoriaRepository;
import cl.veterinaria.sanmarcos.repository.ProductoRepository;
import cl.veterinaria.sanmarcos.repository.UsuarioRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    public DataInitializer(CategoriaRepository categorias, ProductoRepository productos,
                           UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categorias.count() == 0) {
            List.of("Antibióticos", "Antiparasitarios", "Antiinflamatorios", "Dermatología", "Digestivo",
                    "Cardíaco", "Analgésicos", "Vacunas", "Suplementos")
                    .forEach(nombre -> categorias.save(new Categoria("cat-" + slug(nombre), nombre)));
        }
        if (productos.count() == 0) {
            productos.saveAll(List.of(
                    new Producto("ME001", "Antibióticos", "Amoxibay 250mg", "Amoxicilina", "Blíster 10 comp.", "Perro / Gato", 45, 4200, null, "💊"),
                    new Producto("ME002", "Antibióticos", "Enrox 50mg", "Enrofloxacino", "Blíster 10 comp.", "Perro / Gato", 30, 6800, null, "💊"),
                    new Producto("ME004", "Antiparasitarios", "Nexgard", "Afoxolaner", "Masticable 1 unid.", "Perro", 60, 9500, 8500, "🐛"),
                    new Producto("ME005", "Antiparasitarios", "Bravecto", "Fluralaner", "Masticable 1 unid.", "Perro", 40, 18900, 16900, "🐛"),
                    new Producto("ME006", "Antiparasitarios", "Revolution Plus", "Selamectina + Sarolaner", "Pipeta 1 unid.", "Gato", 35, 14500, null, "🐛"),
                    new Producto("ME009", "Antiinflamatorios", "Meloxicam 1mg", "Meloxicam", "Blíster 10 comp.", "Perro / Gato", 55, 4500, null, "🩹"),
                    new Producto("ME011", "Dermatología", "Clorhexidina shampoo", "Clorhexidina 2%", "Frasco 250ml", "Perro / Gato", 25, 8900, null, "🧴"),
                    new Producto("ME013", "Dermatología", "Apoquel 16mg", "Oclacitinib", "Blíster 10 comp.", "Perro", 18, 22000, null, "🧴"),
                    new Producto("ME014", "Digestivo", "Probifor", "Bacillus clausii", "Sobre 5ml x10", "Perro / Gato", 40, 5600, null, "🍽️"),
                    new Producto("ME016", "Cardíaco", "Vetmedin 2.5mg", "Pimobendan", "Blíster 10 comp.", "Perro", 15, 28000, null, "❤️"),
                    new Producto("ME018", "Vacunas", "Nobivac DHPPi", "Vacuna polivalente", "Vial 1 dosis", "Perro", 48, 8500, 7900, "💉"),
                    new Producto("ME021", "Suplementos", "Omega vet 3-6-9", "Ácidos grasos omega", "Frasco 100ml", "Perro / Gato", 30, 9900, null, "🌿")
            ));
        }
        if (!usuarios.existsByCorreoIgnoreCase("admin@duoc.cl"))
            usuarios.save(usuario("demo-admin", "Admin", "San Marcos", "admin@duoc.cl", "Admin123", Rol.ADMINISTRADOR));
        if (!usuarios.existsByCorreoIgnoreCase("vendedor@duoc.cl"))
            usuarios.save(usuario("demo-vendedor", "Vendedor", "San Marcos", "vendedor@duoc.cl", "Vendedor123", Rol.VENDEDOR));
        if (!usuarios.existsByCorreoIgnoreCase("cliente@duoc.cl"))
            usuarios.save(usuario("demo-cliente", "Cliente", "San Marcos", "cliente@duoc.cl", "Cliente123", Rol.CLIENTE));
    }

    private Usuario usuario(String id, String nombre, String apellido, String correo, String password, Rol rol) {
        return new Usuario(id, nombre, apellido, "", correo, encoder.encode(password), "", "", "", rol);
    }
    private String slug(String nombre) {
        return nombre.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
