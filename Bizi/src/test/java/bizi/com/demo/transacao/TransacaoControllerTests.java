package bizi.com.demo.transacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;

@ExtendWith(MockitoExtension.class)
class TransacaoControllerTest {

    @Mock
    private TransacaoService transacaoService;

    @InjectMocks
    private TransacaoController controller;

    // =========================================================
    //  GET /api/transacoes/conta/{idConta} - buscarPorConta
    // =========================================================

    @Test
    @DisplayName("buscarPorConta: deve retornar 200 com transacoes da conta")
    void buscarPorConta_deveRetornar200ComTransacoes() {
        List<TransacaoModel> transacoes = List.of(new TransacaoModel(), new TransacaoModel());
        when(transacaoService.buscarPorConta(1L)).thenReturn(transacoes);

        ResponseEntity<?> response = controller.buscarPorConta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(transacoes);
    }

    @Test
    @DisplayName("buscarPorConta: deve retornar 403 quando acesso negado")
    void buscarPorConta_deveRetornar403QuandoAcessoNegado() {
        when(transacaoService.buscarPorConta(1L))
                .thenThrow(new AccessDeniedException("Acesso negado."));

        ResponseEntity<?> response = controller.buscarPorConta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        var body = (TransacaoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).isEqualTo("Acesso negado.");
    }

    @Test
    @DisplayName("buscarPorConta: deve retornar 404 quando conta nao encontrada")
    void buscarPorConta_deveRetornar404QuandoContaNaoEncontrada() {
        when(transacaoService.buscarPorConta(99L))
                .thenThrow(new ContaBancariaNotFoundException("Conta nao encontrada"));

        ResponseEntity<?> response = controller.buscarPorConta(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var body = (TransacaoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).isEqualTo("Conta nao encontrada");
    }

    @Test
    @DisplayName("buscarPorConta: deve retornar 500 quando ocorre erro inesperado")
    void buscarPorConta_deveRetornar500QuandoErroInesperado() {
        when(transacaoService.buscarPorConta(1L)).thenThrow(new RuntimeException("falha"));

        ResponseEntity<?> response = controller.buscarPorConta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        var body = (TransacaoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("Erro ao buscar transações", "falha");
    }

    // =========================================================
    //  GET /api/transacoes/extrato - exibirExtrato
    // =========================================================

    @Test
    @DisplayName("exibirExtrato: deve retornar 200 com extrato completo")
    void exibirExtrato_deveRetornar200ComExtrato() {
        List<TransacaoModel> extrato = List.of(new TransacaoModel());
        when(transacaoService.listarExtratoCompleto()).thenReturn(extrato);

        ResponseEntity<?> response = controller.exibirExtrato();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(extrato);
    }

    @Test
    @DisplayName("exibirExtrato: deve retornar 500 quando service lanca excecao")
    void exibirExtrato_deveRetornar500QuandoErro() {
        when(transacaoService.listarExtratoCompleto()).thenThrow(new RuntimeException("erro banco"));

        ResponseEntity<?> response = controller.exibirExtrato();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        var body = (TransacaoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("Erro ao gerar extrato", "erro banco");
    }

    // =========================================================
    //  POST /api/transacoes - criarTransacao
    // =========================================================

    @Test
    @DisplayName("criarTransacao: deve retornar 201 quando transacao criada")
    void criarTransacao_deveRetornar201QuandoCriada() {
        TransacaoDto dto = org.mockito.Mockito.mock(TransacaoDto.class);
        TransacaoModel transacao = new TransacaoModel();
        when(transacaoService.criarTransacao(dto)).thenReturn(transacao);

        ResponseEntity<?> response = controller.criarTransacao(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(transacao);
    }

    @Test
    @DisplayName("criarTransacao: deve retornar 403 quando acesso negado")
    void criarTransacao_deveRetornar403QuandoAcessoNegado() {
        TransacaoDto dto = org.mockito.Mockito.mock(TransacaoDto.class);
        when(transacaoService.criarTransacao(dto))
                .thenThrow(new AccessDeniedException("Conta de outro usuario"));

        ResponseEntity<?> response = controller.criarTransacao(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        var body = (TransacaoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).isEqualTo("Conta de outro usuario");
    }

    @Test
    @DisplayName("criarTransacao: deve retornar 400 quando ocorre erro de validacao/regra")
    void criarTransacao_deveRetornar400QuandoErro() {
        TransacaoDto dto = org.mockito.Mockito.mock(TransacaoDto.class);
        when(transacaoService.criarTransacao(dto)).thenThrow(new RuntimeException("valor invalido"));

        ResponseEntity<?> response = controller.criarTransacao(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = (TransacaoController.ErrorResponse) response.getBody();
        assertThat(body.getMensagem()).contains("Erro ao processar transação", "valor invalido");
    }

    // =========================================================
    //  GET /api/transacoes - listarTodas
    // =========================================================

    @Test
    @DisplayName("listarTodas: deve retornar 200 com todas as transacoes")
    void listarTodas_deveRetornar200ComTransacoes() {
        List<TransacaoModel> transacoes = List.of(new TransacaoModel());
        when(transacaoService.listarTodas()).thenReturn(transacoes);

        ResponseEntity<List<TransacaoModel>> response = controller.listarTodas();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(transacoes);
    }

    // =========================================================
    //  DELETE /api/transacoes/{id} - deletarTransacao
    // =========================================================

    @Test
    @DisplayName("deletarTransacao: deve retornar 204 quando removida")
    void deletarTransacao_deveRetornar204QuandoRemovida() {
        doNothing().when(transacaoService).deletarTransacao(1L);

        ResponseEntity<?> response = controller.deletarTransacao(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("deletarTransacao: deve retornar 404 quando service lanca excecao")
    void deletarTransacao_deveRetornar404QuandoErro() {
        doThrow(new RuntimeException("ID invalido")).when(transacaoService).deletarTransacao(99L);

        ResponseEntity<?> response = controller.deletarTransacao(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
