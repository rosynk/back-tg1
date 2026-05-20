package bizi.com.demo.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService service = new JwtService();

    @Test
    @DisplayName("gerarToken: deve gerar token válido")
    void gerarToken_deveGerarTokenValido() {
        String token = service.gerarToken("usuario@teste.com", "ROLE_CLIENTE");

        assertThat(token).isNotBlank();
        assertThat(service.tokenValido(token)).isTrue();
    }

    @Test
    @DisplayName("extrairEmail: deve retornar subject do token")
    void extrairEmail_deveRetornarEmail() {
        String token = service.gerarToken("usuario@teste.com", "ROLE_CLIENTE");

        String email = service.extrairEmail(token);

        assertThat(email).isEqualTo("usuario@teste.com");
    }

    @Test
    @DisplayName("extrairRole: deve retornar role do token")
    void extrairRole_deveRetornarRole() {
        String token = service.gerarToken("usuario@teste.com", "ROLE_ADMIN");

        String role = service.extrairRole(token);

        assertThat(role).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("tokenValido: deve retornar false para token inválido")
    void tokenValido_deveRetornarFalseParaTokenInvalido() {
        boolean resultado = service.tokenValido("token-invalido");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("extrairClaim: deve extrair claim customizada")
    void extrairClaim_deveExtrairClaimCustomizada() {
        String token = service.gerarToken("usuario@teste.com", "ROLE_CLIENTE");

        String subject = service.extrairClaim(token, claims -> claims.getSubject());

        assertThat(subject).isEqualTo("usuario@teste.com");
    }
}