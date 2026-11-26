package cl.veterinaria.sanmarcos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cl.veterinaria.sanmarcos.dto.LoginRequest;
import cl.veterinaria.sanmarcos.model.Rol;
import cl.veterinaria.sanmarcos.model.Usuario;
import cl.veterinaria.sanmarcos.repository.UsuarioRepository;
import cl.veterinaria.sanmarcos.repository.RefreshTokenRepository;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class VeterinariaIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void crearUsuario() {
        refreshTokens.deleteAll();
        usuarios.deleteAll();
        Usuario usuario = new Usuario("usr-" + UUID.randomUUID(), "Ana", "Prueba", "2000-01-01",
                "ana@duoc.cl", encoder.encode("Ana12345"), "Calle 1", "Metropolitana", "Otro",
                Rol.ADMINISTRADOR);
        usuarios.save(usuario);
        usuarios.save(new Usuario("seller-test", "Sofía", "Vendedora", "2000-01-01",
                "sofia@duoc.cl", encoder.encode("Sofia12345"), "Calle 2", "Metropolitana", "Otro",
                Rol.VENDEDOR));
    }

    @Test
    void healthNoRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void catalogosVersionadosNoRequierenAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    void rechazaAccesoProtegidoSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restringeListadoDeUsuariosAlRolAdministrador() throws Exception {
        Cookie adminCookie = iniciarSesion("ana@duoc.cl", "Ana12345");
        Cookie sellerCookie = iniciarSesion("sofia@duoc.cl", "Sofia12345");

        mockMvc.perform(get("/api/v1/usuarios").cookie(adminCookie))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/usuarios").cookie(sellerCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginEntregaCookiesHttpOnlySinDevolverTokensEnJson() throws Exception {
        MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest("ana@duoc.cl", "Ana12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.correo").value("ana@duoc.cl"))
                .andReturn();
        var cookies = result.getResponse().getHeaders("Set-Cookie");
        org.junit.jupiter.api.Assertions.assertTrue(cookies.stream().anyMatch(value ->
                value.startsWith("vsm_access=") && value.contains("HttpOnly") && value.contains("SameSite=Lax")));
        org.junit.jupiter.api.Assertions.assertTrue(cookies.stream().anyMatch(value ->
                value.startsWith("vsm_refresh=") && value.contains("HttpOnly") && value.contains("Path=/api/v1/auth")));
    }

    @Test
    void loginIncorrectoDevuelveUnauthorized() throws Exception {
        mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest("ana@duoc.cl", "Incorrecta1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mutacionesExigenTokenCsrfYLogoutLimpiaCookies() throws Exception {
        MvcResult login = mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest("ana@duoc.cl", "Ana12345"))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie accessCookie = login.getResponse().getCookie("vsm_access");
        Cookie refreshCookie = login.getResponse().getCookie("vsm_refresh");
        mockMvc.perform(post("/api/v1/auth/logout").cookie(accessCookie))
                .andExpect(status().isForbidden());

        MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/auth/logout"))
                        .cookie(accessCookie, refreshCookie))
                .andExpect(status().isOk())
                .andReturn();
        var cookies = result.getResponse().getHeaders("Set-Cookie");
        org.junit.jupiter.api.Assertions.assertTrue(cookies.stream().anyMatch(value ->
                value.startsWith("vsm_access=;") && value.contains("Max-Age=0")));
        org.junit.jupiter.api.Assertions.assertTrue(cookies.stream().anyMatch(value ->
                value.startsWith("vsm_refresh=;") && value.contains("Max-Age=0")));
        mockMvc.perform(withCsrf(post("/api/v1/auth/refresh")).cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotaCookiesYPermiteSeguirAutenticado() throws Exception {
        MvcResult login = mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest("ana@duoc.cl", "Ana12345"))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie accessCookie = login.getResponse().getCookie("vsm_access");
        Cookie refreshCookie = login.getResponse().getCookie("vsm_refresh");

        MvcResult refresh = mockMvc.perform(withCsrf(post("/api/v1/auth/refresh"))
                        .cookie(accessCookie, refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.correo").value("ana@duoc.cl"))
                .andReturn();
        Cookie rotatedAccess = refresh.getResponse().getCookie("vsm_access");
        Cookie rotatedRefresh = refresh.getResponse().getCookie("vsm_refresh");
        org.junit.jupiter.api.Assertions.assertNotNull(rotatedAccess);
        org.junit.jupiter.api.Assertions.assertNotNull(rotatedRefresh);
        org.junit.jupiter.api.Assertions.assertNotEquals(refreshCookie.getValue(), rotatedRefresh.getValue());
        mockMvc.perform(get("/api/v1/auth/me").cookie(rotatedAccess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@duoc.cl"));
    }

    private Cookie iniciarSesion(String correo, String password) throws Exception {
        MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest(correo, password))))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("vsm_access");
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        if (csrfCookie == null) throw new AssertionError("El endpoint CSRF no entregó la cookie XSRF-TOKEN.");
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        return request.cookie(csrfCookie).header("X-XSRF-TOKEN", token);
    }
}
