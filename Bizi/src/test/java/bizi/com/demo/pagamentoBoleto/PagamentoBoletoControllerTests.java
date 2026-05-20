package bizi.com.demo.pagamentoBoleto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.usuario.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class PagamentoBoletoControllerTest {

    @Mock
    private PagamentoBoletoService pagamentoBoletoService;

    @InjectMocks
    private PagamentoBoletoController controller;

    private PagamentoBoletoModel pagamento;
    private PagamentoBoletoDto pagamentoDto;

    @BeforeEach
    void setUp() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setNomeCompleto("João Silva");
        usuario.setCpf("12345678900");

        ContaBancariaModel conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setUsuario(usuario);

        TransacaoModel transacao = new TransacaoModel();
        transacao.setId(100L);
        transacao.setValor(new BigDecimal("150.00"));
        transacao.setContaBancaria(conta);

        pagamento = new PagamentoBoletoModel();
        pagamento.setId(1L);
        pagamento.setTransacao(transacao);
        pagamento.setCodigoBarras("34191.23456 78901.234567 89012.345678 1 12340000015000");
        pagamento.setNomeBeneficiario("Empresa Teste LTDA");

        pagamentoDto = new PagamentoBoletoDto(pagamento);
        pagamentoDto.setCodigoBarras("34191.23456 78901.234567 89012.345678 1 12340000015000");
        pagamentoDto.setValor(new BigDecimal("150.00"));
        pagamentoDto.setNomeBeneficiario("Empresa Teste LTDA");
    }

    // =========================================================
    //  POST /api/pagamentos/boleto - realizarPagamento
    // =========================================================

    @Test
    @DisplayName("realizarPagamento: deve retornar 201 com pagamento quando sucesso")
    void realizarPagamento_deveRetornar201QuandoSucesso() {
        when(pagamentoBoletoService.realizarPagamento(any())).thenReturn(pagamento);

        ResponseEntity<?> response = controller.realizarPagamento(pagamentoDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(pagamento);
    }

    @Test
    @DisplayName("realizarPagamento: deve retornar 400 quando PagamentoBoletoException lançada")
    void realizarPagamento_deveRetornar400QuandoPagamentoBoletoException() {
        when(pagamentoBoletoService.realizarPagamento(any()))
                .thenThrow(new PagamentoBoletoException("Saldo insuficiente."));

        ResponseEntity<?> response = controller.realizarPagamento(pagamentoDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        PagamentoBoletoController.ErrorResponse body =
                (PagamentoBoletoController.ErrorResponse) response.getBody();

        assertThat(body.getMensagem()).isEqualTo("Saldo insuficiente.");
    }

    @Test
    @DisplayName("realizarPagamento: deve retornar 500 quando exceção genérica lançada")
    void realizarPagamento_deveRetornar500QuandoExcecaoGenerica() {
        when(pagamentoBoletoService.realizarPagamento(any()))
                .thenThrow(new RuntimeException("Erro inesperado no banco de dados."));

        ResponseEntity<?> response = controller.realizarPagamento(pagamentoDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        PagamentoBoletoController.ErrorResponse body =
                (PagamentoBoletoController.ErrorResponse) response.getBody();

        assertThat(body.getMensagem()).contains("Erro ao processar pagamento");
        assertThat(body.getMensagem()).contains("Erro inesperado no banco de dados.");
    }

    // =========================================================
    //  GET /api/pagamentos/boleto/{id} - buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar 200 com DTO quando pagamento encontrado")
    void buscarPorId_deveRetornar200QuandoEncontrado() {
        when(pagamentoBoletoService.buscarPorId(1L)).thenReturn(pagamentoDto);

        ResponseEntity<PagamentoBoletoDto> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pagamentoDto);
    }

    @Test
    @DisplayName("buscarPorId: deve propagar exceção quando pagamento não encontrado")
    void buscarPorId_devePropagarExcecaoQuandoNaoEncontrado() {
        when(pagamentoBoletoService.buscarPorId(999L))
                .thenThrow(new RuntimeException("Pagamento com ID 999 não encontrado no BiziBanco."));

        assertThatThrownBy(() -> controller.buscarPorId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    // =========================================================
    //  GET /api/pagamentos/boleto/conta/{idConta} - buscarPorConta
    // =========================================================

    @Test
    @DisplayName("buscarPorConta: deve retornar 200 com lista de pagamentos da conta")
    void buscarPorConta_deveRetornar200ComListaDePagamentos() {
        when(pagamentoBoletoService.buscarPorConta(10L)).thenReturn(List.of(pagamentoDto));

        ResponseEntity<List<PagamentoBoletoDto>> response = controller.buscarPorConta(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0)).isEqualTo(pagamentoDto);
    }

    @Test
    @DisplayName("buscarPorConta: deve retornar 200 com lista vazia quando conta não tem pagamentos")
    void buscarPorConta_deveRetornar200ComListaVazia() {
        when(pagamentoBoletoService.buscarPorConta(anyLong())).thenReturn(List.of());

        ResponseEntity<List<PagamentoBoletoDto>> response = controller.buscarPorConta(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("buscarPorConta: deve retornar 200 com múltiplos pagamentos")
    void buscarPorConta_deveRetornar200ComMultiplosPagamentos() {
        PagamentoBoletoDto dto2 = new PagamentoBoletoDto(pagamento);
        dto2.setCodigoBarras("outro-codigo");
        dto2.setValor(new BigDecimal("300.00"));
        dto2.setNomeBeneficiario("Outra Empresa");

        when(pagamentoBoletoService.buscarPorConta(10L)).thenReturn(List.of(pagamentoDto, dto2));

        ResponseEntity<List<PagamentoBoletoDto>> response = controller.buscarPorConta(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}