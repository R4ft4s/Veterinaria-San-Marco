package cl.veterinaria.sanmarcos.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

public enum Rol implements GrantedAuthority {
    ADMINISTRADOR("administrador"), VENDEDOR("vendedor"), CLIENTE("cliente");

    private final String valor;

    Rol(String valor) { this.valor = valor; }

    @JsonValue
    public String getValor() { return valor; }

    @JsonCreator
    public static Rol from(String valor) {
        for (Rol rol : values()) if (rol.valor.equalsIgnoreCase(valor) || rol.name().equalsIgnoreCase(valor)) return rol;
        throw new IllegalArgumentException("Rol no válido.");
    }

    @Override
    public String getAuthority() { return "ROLE_" + name(); }
}
