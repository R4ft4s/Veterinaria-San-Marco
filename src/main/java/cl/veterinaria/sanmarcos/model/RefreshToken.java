package cl.veterinaria.sanmarcos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @Column(length = 64)
    private String token;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;
    @Column(nullable = false)
    private boolean revoked;

    protected RefreshToken() {}

    public RefreshToken(String token, Usuario usuario, Instant expiraEn) {
        this.token = token;
        this.usuario = usuario;
        this.expiraEn = expiraEn;
    }

    public String getToken() { return token; }
    public Usuario getUsuario() { return usuario; }
    public Instant getExpiraEn() { return expiraEn; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
