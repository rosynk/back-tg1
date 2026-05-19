package bizi.com.demo.pix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.validacoes.external.ChavePixApiResponse;

@ExtendWith(MockitoExtension.class)
class PixControllerTest {

    @Mock private PixService pixService;

    @InjectMocks
    private PixController controller;

    private PixModel pixModel;

    @BeforeEach
    void setUp() {
        pixModel = new PixModel();
        pixModel.setId(1L);
        pixModel.setChavePixDestino("maria@email.com");
        pixModel.setMensagem("Pagamento teste");
    }

    // =========================================================
    //  GET /api/pix/conta — getDadosConta
    // =========================================================

    @Test
    @DisplayName("getDadosConta: deve retornar 200 com dados da conta do usuário logado")
    void getDadosConta_deveRetornar200ComDadosDaConta() {
        Map<String, Object> resumo = Map.of(
                "saldo", new BigDecimal("1000.00"),
                "numeroConta", "111111",
                "numeroAgencia", "0001",
                "extrato", List.of()
        );
        when(pixService.buscarInformacoesResumo()).thenReturn(resumo);

        ResponseEntity<?> response = controller.getDadosConta();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = (Map<?, ?>) response.getBody();
        assertThat(body.get("saldo")).isEqualTo(new BigDecimal("1000.00"));
        assertThat(body.get("numeroConta")).isEqualTo("111111");
    }

    @Test
    @DisplayName("getDadosConta: deve retornar 200 com valores zerados quando usuário não tem conta")
    void getDadosConta_deveRetornar200ComValoresZeradosQuandoSemConta() {
        Map<String, Object> resumoVazio = Map.of(
                "saldo", BigDecimal.ZERO,
                "numeroConta", "Pendente",
                "agencia", "0000",
                "extrato", List.of()
        );
        when(pixService.buscarInformacoesResumo()).thenReturn(resumoVazio);

        ResponseEntity<?> response = controller.getDadosConta();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = (Map<?, ?>) response.getBody();
        assertThat(body.get("saldo")).isEqualTo(BigDecimal.ZERO);
        assertThat(body.get("numeroConta")).isEqualTo("Pendente");
    }

    // =========================================================
    //  POST /api/pix/transferir — enviar
    // =========================================================

    @Test
    @DisplayName("enviar: deve retornar 200 com sucesso quando Pix realizado")
    void enviar_deveRetornar200QuandoSucesso() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("200.00"));

        when(pixService.realizarPix(any())).thenReturn(pixModel);

        ResponseEntity<ChavePixApiResponse> response = controller.enviar(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSucesso()).isTrue();
        assertThat(response.getBody().getMensagem()).contains("sucesso");
        assertThat(response.getBody().getDados()).isEqualTo(pixModel);
    }

    @Test
    @DisplayName("enviar: deve retornar 400 quando saldo insuficiente")
    void enviar_deveRetornar400QuandoSaldoInsuficiente() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("9999.00"));

        when(pixService.realizarPix(any()))
                .thenThrow(new RuntimeException("Saldo insuficiente para concluir o Pix."));

        ResponseEntity<ChavePixApiResponse> response = controller.enviar(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("Saldo insuficiente");
        assertThat(response.getBody().getDados()).isNull();
    }

    @Test
    @DisplayName("enviar: deve retornar 400 quando chave Pix não encontrada")
    void enviar_deveRetornar400QuandoChaveNaoEncontrada() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("inexistente@email.com");
        dto.setValor(new BigDecimal("100.00"));

        when(pixService.realizarPix(any()))
                .thenThrow(new RuntimeException("Chave Pix não encontrada no sistema."));

        ResponseEntity<ChavePixApiResponse> response = controller.enviar(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("Chave Pix não encontrada");
    }

    @Test
    @DisplayName("enviar: deve retornar 400 quando tentativa de Pix para a própria conta")
    void enviar_deveRetornar400QuandoPixParaPropriaContaA() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("joao@email.com");
        dto.setValor(new BigDecimal("100.00"));

        when(pixService.realizarPix(any()))
                .thenThrow(new RuntimeException("Não é possível realizar um Pix para a própria conta."));

        ResponseEntity<ChavePixApiResponse> response = controller.enviar(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("própria conta");
    }

    // =========================================================
    //  GET /api/pix/historico — listarHistorico
    // =========================================================

    @Test
    @DisplayName("listarHistorico: deve retornar 200 com lista de Pix do usuário logado")
    void listarHistorico_deveRetornar200ComHistorico() {
        when(pixService.listarPixDoUsuarioLogado()).thenReturn(List.of(pixModel));

        ResponseEntity<ChavePixApiResponse> response = controller.listarHistorico();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSucesso()).isTrue();
        assertThat(response.getBody().getMensagem()).contains("Histórico recuperado");
        var dados = (List<?>) response.getBody().getDados();
        assertThat(dados).hasSize(1);
    }

    @Test
    @DisplayName("listarHistorico: deve retornar 200 com lista vazia quando não há histórico")
    void listarHistorico_deveRetornar200ComListaVazia() {
        when(pixService.listarPixDoUsuarioLogado()).thenReturn(List.of());

        ResponseEntity<ChavePixApiResponse> response = controller.listarHistorico();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSucesso()).isTrue();
        var dados = (List<?>) response.getBody().getDados();
        assertThat(dados).isEmpty();
    }

    @Test
    @DisplayName("listarHistorico: deve retornar 500 quando ocorre erro inesperado")
    void listarHistorico_deveRetornar500QuandoErroInesperado() {
        when(pixService.listarPixDoUsuarioLogado())
                .thenThrow(new RuntimeException("Falha na conexão com o banco."));

        ResponseEntity<ChavePixApiResponse> response = controller.listarHistorico();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("Erro ao buscar histórico");
        assertThat(response.getBody().getDados()).isNull();
    }
}