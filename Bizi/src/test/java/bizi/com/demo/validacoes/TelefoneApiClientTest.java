package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import bizi.com.demo.validacoes.external.TelefoneApiClient;

class TelefoneApiClientTest {

    private TelefoneApiClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new TelefoneApiClient();

        Field field = TelefoneApiClient.class.getDeclaredField("restTemplate");
        field.setAccessible(true);

        RestTemplate restTemplate = (RestTemplate) field.get(client);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("validarTelefone: deve retornar true quando API retorna valid true")
    void validarTelefone_deveRetornarTrueQuandoApiRetornaValidTrue() {
        server.expect(requestTo("https://api.exemplo.com/telefone?numero=11999999999"))
                .andRespond(withSuccess("{\"valid\": true}", MediaType.APPLICATION_JSON));

        boolean resultado = client.validarTelefone("11999999999");

        assertThat(resultado).isTrue();

        server.verify();
    }

    @Test
    @DisplayName("validarTelefone: deve retornar false quando API retorna valid false")
    void validarTelefone_deveRetornarFalseQuandoApiRetornaValidFalse() {
        server.expect(requestTo("https://api.exemplo.com/telefone?numero=11000000000"))
                .andRespond(withSuccess("{\"valid\": false}", MediaType.APPLICATION_JSON));

        boolean resultado = client.validarTelefone("11000000000");

        assertThat(resultado).isFalse();

        server.verify();
    }

    @Test
    @DisplayName("validarTelefone: deve retornar false quando API retorna body vazio")
    void validarTelefone_deveRetornarFalseQuandoApiRetornaBodyVazio() {
        server.expect(requestTo("https://api.exemplo.com/telefone?numero=11999999999"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        boolean resultado = client.validarTelefone("11999999999");

        assertThat(resultado).isFalse();

        server.verify();
    }
}