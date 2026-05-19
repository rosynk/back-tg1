package bizi.com.demo.endereco;

import bizi.com.demo.validacoes.external.ViaCepClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoControllerTest {

    @Mock private EnderecoService enderecoService;

    @InjectMocks
    private EnderecoController controller;

    private EnderecoModel endereco;

    @BeforeEach
    void setUp() {
        endereco = new EnderecoModel();
        endereco.setId(1L);
        endereco.setCep("01310100");
        endereco.setRua("Avenida Paulista");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setNumero(1000);
        endereco.setComplemento("Apto 42");
    }

    // =========================================================
    //  POST /api/enderecos — criarEndereco
    // =========================================================

    @Test
    @DisplayName("criarEndereco: deve retornar 201 quando endereço criado com sucesso")
    void criarEndereco_deveRetornar201QuandoSucesso() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01310100");
        dto.setNumero(1000);

        when(enderecoService.criarEndereco(dto)).thenReturn(endereco);

        ResponseEntity<?> response = controller.criarEndereco(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(endereco);
    }

    @Test
    @DisplayName("criarEndereco: deve retornar 400 quando CEP inválido")
    void criarEndereco_deveRetornar400QuandoCepInvalido() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("00000000");

        when(enderecoService.criarEndereco(dto))
                .thenThrow(new RuntimeException("CEP não encontrado na base do ViaCEP."));

        ResponseEntity<?> response = controller.criarEndereco(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = (EnderecoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("CEP não encontrado");
    }

    // =========================================================
    //  GET /api/enderecos/{id} — buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar 200 com endereço quando encontrado")
    void buscarPorId_deveRetornar200QuandoEncontrado() {
        when(enderecoService.buscarPorId(1L)).thenReturn(endereco);

        ResponseEntity<?> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(endereco);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar 404 quando endereço não existe")
    void buscarPorId_deveRetornar404QuandoNaoEncontrado() {
        when(enderecoService.buscarPorId(999L))
                .thenThrow(new EnderecoNotFoundException("Endereço não encontrado com ID: 999"));

        ResponseEntity<?> response = controller.buscarPorId(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar 403 quando usuário não tem permissão")
    void buscarPorId_deveRetornar403QuandoAcessoNegado() {
        when(enderecoService.buscarPorId(1L))
                .thenThrow(new AccessDeniedException("Acesso negado."));

        ResponseEntity<?> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        var body = (EnderecoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).isEqualTo("Acesso negado.");
    }

    // =========================================================
    //  PUT /api/enderecos/{id} — atualizarEndereco
    // =========================================================

    @Test
    @DisplayName("atualizarEndereco: deve retornar 200 quando atualizado com sucesso")
    void atualizarEndereco_deveRetornar200QuandoSucesso() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01310100");
        dto.setNumero(2000);

        when(enderecoService.atualizarEndereco(1L, dto)).thenReturn(endereco);

        ResponseEntity<?> response = controller.atualizarEndereco(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(endereco);
    }

    @Test
    @DisplayName("atualizarEndereco: deve retornar 404 quando endereço não existe")
    void atualizarEndereco_deveRetornar404QuandoNaoEncontrado() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("01310100");

        when(enderecoService.atualizarEndereco(eq(999L), any()))
                .thenThrow(new EnderecoNotFoundException("Endereço não encontrado com ID: 999"));

        ResponseEntity<?> response = controller.atualizarEndereco(999L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("atualizarEndereco: deve retornar 400 quando novo CEP é inválido")
    void atualizarEndereco_deveRetornar400QuandoCepInvalido() {
        EnderecoDto dto = new EnderecoDto();
        dto.setCep("00000000");

        when(enderecoService.atualizarEndereco(eq(1L), any()))
                .thenThrow(new RuntimeException("CEP não encontrado na base do ViaCEP."));

        ResponseEntity<?> response = controller.atualizarEndereco(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = (EnderecoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("CEP não encontrado");
    }

    // =========================================================
    //  GET /api/enderecos/consultar-cep/{cep} — consultarViaCep
    // =========================================================

    @Test
    @DisplayName("consultarViaCep: deve retornar 200 com dados do CEP quando válido")
    void consultarViaCep_deveRetornar200ComDadosQuandoCepValido() {
        ViaCepClient.ViaCepResponse viaCepResponse = new ViaCepClient.ViaCepResponse();
        viaCepResponse.setLogradouro("Avenida Paulista");
        viaCepResponse.setLocalidade("São Paulo");
        viaCepResponse.setUf("SP");

        when(enderecoService.consultarCepExterno("01310100")).thenReturn(viaCepResponse);

        ResponseEntity<?> response = controller.consultarViaCep("01310100");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(viaCepResponse);
    }

    @Test
    @DisplayName("consultarViaCep: deve retornar 400 quando CEP inválido")
    void consultarViaCep_deveRetornar400QuandoCepInvalido() {
        when(enderecoService.consultarCepExterno("00000000"))
                .thenThrow(new RuntimeException("CEP não encontrado na base do ViaCEP."));

        ResponseEntity<?> response = controller.consultarViaCep("00000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = (EnderecoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("CEP não encontrado");
    }

    // =========================================================
    //  DELETE /api/enderecos/{id} — deletarEndereco
    // =========================================================

    @Test
    @DisplayName("deletarEndereco: deve retornar 204 quando deletado com sucesso")
    void deletarEndereco_deveRetornar204QuandoSucesso() {
        doNothing().when(enderecoService).deletarEndereco(1L);

        ResponseEntity<?> response = controller.deletarEndereco(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("deletarEndereco: deve retornar 404 quando endereço não existe")
    void deletarEndereco_deveRetornar404QuandoNaoExiste() {
        doThrow(new EnderecoNotFoundException("ID inexistente."))
                .when(enderecoService).deletarEndereco(999L);

        ResponseEntity<?> response = controller.deletarEndereco(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================
    //  GET /api/enderecos — listarTodos
    // =========================================================

    @Test
    @DisplayName("listarTodos: deve retornar 200 com lista de endereços")
    void listarTodos_deveRetornar200ComListaDeEnderecos() {
        when(enderecoService.listarTodos()).thenReturn(List.of(endereco, new EnderecoModel()));

        ResponseEntity<List<EnderecoModel>> response = controller.listarTodos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("listarTodos: deve retornar 200 com lista vazia quando não há endereços")
    void listarTodos_deveRetornar200ComListaVazia() {
        when(enderecoService.listarTodos()).thenReturn(List.of());

        ResponseEntity<List<EnderecoModel>> response = controller.listarTodos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}