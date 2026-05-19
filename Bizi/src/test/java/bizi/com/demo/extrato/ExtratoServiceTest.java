package bizi.com.demo.extrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoService;
import bizi.com.demo.usuario.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class ExtratoServiceTest {

    @Mock private TransacaoService transacaoService;
    @Mock private ContaBancariaService contaService;
    @Mock private ContaBancariaRepository contaBancariaRepository;

    @InjectMocks
    private ExtratoService service;

    private ContaBancariaModel conta;
    private UsuarioModel usuario;

    // Período padrão usado nos testes
    private final LocalDate INICIO = LocalDate.of(2025, 1, 1);
    private final LocalDate FIM    = LocalDate.of(2025, 1, 31);

    @BeforeEach
    void setUp() {
        usuario = new UsuarioModel();
        usuario.setNomeCompleto("João Silva");

        conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setUsuario(usuario);
        conta.setSaldo(new BigDecimal("2000.00"));
        conta.setNumeroConta("123456");
        conta.setNumeroAgencia("0001");
    }

    // --- helpers para montar transações de teste ---

    private TransacaoModel transacao(TipoTransacao tipo, BigDecimal valor, LocalDateTime dataHora, String contraparte) {
        TransacaoModel t = new TransacaoModel();
        t.setTipoTransacao(tipo);
        t.setValor(valor);
        t.setDataHora(dataHora);
        t.setNomeContraparte(contraparte);
        return t;
    }

    // =========================================================
    //  gerarExtrato
    // =========================================================

    @Test
    @DisplayName("gerarExtrato: deve retornar extrato com transações no período")
    void gerarExtrato_deveRetornarExtratoComTransacoesNoPeriodo() {
        TransacaoModel dentro = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("500.00"),
            LocalDateTime.of(2025, 1, 15, 10, 0),
            "Maria"
        );
        TransacaoModel fora = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("100.00"),
            LocalDateTime.of(2025, 2, 5, 10, 0), // fora do período
            "Carlos"
        );

        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(dentro, fora));

        ExtratoResponseDto resultado = service.gerarExtrato(INICIO, FIM);

        assertThat(resultado.getTitular()).isEqualTo("João Silva");
        assertThat(resultado.getSaldoAtual()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(resultado.getTransacoes()).hasSize(1);
    }

    @Test
    @DisplayName("gerarExtrato: deve retornar lista vazia quando não há transações no período")
    void gerarExtrato_deveRetornarListaVaziaQuandoSemTransacoesNoPeriodo() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of());

        ExtratoResponseDto resultado = service.gerarExtrato(INICIO, FIM);

        assertThat(resultado.getTransacoes()).isEmpty();
    }

    @Test
    @DisplayName("gerarExtrato: deve lançar exceção quando usuário não tem conta")
    void gerarExtrato_deveLancarExcecaoQuandoUsuarioSemConta() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of());

        assertThatThrownBy(() -> service.gerarExtrato(INICIO, FIM))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("conta bancária ativa");
    }

    @Test
    @DisplayName("gerarExtrato: deve lançar exceção quando buscarMinhasContas retorna null")
    void gerarExtrato_deveLancarExcecaoQuandoContasNull() {
        when(contaService.buscarMinhasContas()).thenReturn(null);

        assertThatThrownBy(() -> service.gerarExtrato(INICIO, FIM))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("conta bancária ativa");
    }

    @Test
    @DisplayName("gerarExtrato: transações devem ser ordenadas da mais recente para a mais antiga")
    void gerarExtrato_deveOrdenarTransacoesDecrescente() {
        TransacaoModel antiga = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("100.00"),
            LocalDateTime.of(2025, 1, 5, 8, 0),
            "Ana"
        );
        TransacaoModel recente = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("200.00"),
            LocalDateTime.of(2025, 1, 20, 8, 0),
            "Bruno"
        );

        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(antiga, recente));

        ExtratoResponseDto resultado = service.gerarExtrato(INICIO, FIM);

        // Primeira da lista deve ser a mais recente (20/jan)
        assertThat(resultado.getTransacoes().get(0).getValor())
                .isEqualByComparingTo(new BigDecimal("200.00"));
    }

    // =========================================================
    //  formatarTransacoes — lógica de crédito/débito
    // =========================================================

    @Test
    @DisplayName("gerarExtrato: transação ENVIADA deve ter valor negativo no extrato")
    void gerarExtrato_transacaoEnviadaDeveSerNegativa() {
        TransacaoModel enviada = transacao(
            TipoTransacao.TRANSFERENCIA_ENVIADA,
            new BigDecimal("300.00"),
            LocalDateTime.of(2025, 1, 10, 12, 0),
            "Loja ABC"
        );

        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(enviada));

        ExtratoResponseDto resultado = service.gerarExtrato(INICIO, FIM);
        ExtratoDto dto = resultado.getTransacoes().get(0);

        assertThat(dto.getValor()).isNegative();
        assertThat(dto.getDetalhes()).contains("PAGTO PARA");
        assertThat(dto.getDetalhes()).contains("Loja ABC");
    }

    @Test
    @DisplayName("gerarExtrato: transação RECEBIDA deve ter valor positivo no extrato")
    void gerarExtrato_transacaoRecebidaDeveSerPositiva() {
        TransacaoModel recebida = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("500.00"),
            LocalDateTime.of(2025, 1, 10, 12, 0),
            "Empresa XYZ"
        );

        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(recebida));

        ExtratoResponseDto resultado = service.gerarExtrato(INICIO, FIM);
        ExtratoDto dto = resultado.getTransacoes().get(0);

        assertThat(dto.getValor()).isPositive();
        assertThat(dto.getDetalhes()).contains("CRED DE");
        assertThat(dto.getDetalhes()).contains("Empresa XYZ");
    }

    @Test
    @DisplayName("gerarExtrato: deve incluir transações nos limites exatos do período (início e fim do dia)")
    void gerarExtrato_deveIncluirTransacoesNosLimitesExatosDoPeriodo() {
        TransacaoModel noInicio = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("100.00"),
            LocalDateTime.of(2025, 1, 1, 0, 0, 0), // exatamente meia-noite do dia 1
            "Teste"
        );
        TransacaoModel noFim = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("200.00"),
            LocalDateTime.of(2025, 1, 31, 23, 59, 59), // último segundo do dia 31
            "Teste"
        );

        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(noInicio, noFim));

        ExtratoResponseDto resultado = service.gerarExtrato(INICIO, FIM);

        assertThat(resultado.getTransacoes()).hasSize(2);
    }

    // =========================================================
    //  gerarExtratoParaAdm
    // =========================================================

    @Test
    @DisplayName("gerarExtratoParaAdm: deve retornar extrato dos últimos 30 dias da conta informada")
    void gerarExtratoParaAdm_deveRetornarExtratoDosUltimos30Dias() {
        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of());

        ExtratoResponseDto resultado = service.gerarExtratoParaAdm(10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitular()).isEqualTo("João Silva");
        verify(contaBancariaRepository).findById(10L);
    }

    @Test
    @DisplayName("gerarExtratoParaAdm: deve lançar exceção quando conta não encontrada")
    void gerarExtratoParaAdm_deveLancarExcecaoQuandoContaNaoEncontrada() {
        when(contaBancariaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerarExtratoParaAdm(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    // =========================================================
    //  gerarExtratoParaAdmComPeriodo
    // =========================================================

    @Test
    @DisplayName("gerarExtratoParaAdmComPeriodo: deve filtrar transações pelo período informado")
    void gerarExtratoParaAdmComPeriodo_deveFiltrarTransacoesPeloPeriodo() {
        TransacaoModel dentro = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("800.00"),
            LocalDateTime.of(2025, 1, 15, 9, 0),
            "Cliente A"
        );
        TransacaoModel fora = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("200.00"),
            LocalDateTime.of(2025, 3, 1, 9, 0),
            "Cliente B"
        );

        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(dentro, fora));

        ExtratoResponseDto resultado = service.gerarExtratoParaAdmComPeriodo(10L, INICIO, FIM);

        assertThat(resultado.getTransacoes()).hasSize(1);
        assertThat(resultado.getTransacoes().get(0).getValor())
                .isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("gerarExtratoParaAdmComPeriodo: deve lançar exceção quando conta não encontrada")
    void gerarExtratoParaAdmComPeriodo_deveLancarExcecaoQuandoContaNaoEncontrada() {
        when(contaBancariaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerarExtratoParaAdmComPeriodo(404L, INICIO, FIM))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    // =========================================================
    //  gerarExtratoPdf — smoke test (verifica que gera bytes sem explodir)
    // =========================================================

    @Test
    @DisplayName("gerarExtratoPdf: deve gerar PDF não vazio com transações de crédito e débito")
    void gerarExtratoPdf_deveGerarPdfNaoVazio() {
        TransacaoModel credito = transacao(
            TipoTransacao.TRANSFERENCIA_RECEBIDA,
            new BigDecimal("500.00"),
            LocalDateTime.of(2025, 1, 10, 10, 0),
            "Empresa"
        );
        TransacaoModel debito = transacao(
            TipoTransacao.TRANSFERENCIA_ENVIADA,
            new BigDecimal("200.00"),
            LocalDateTime.of(2025, 1, 12, 14, 0),
            "Fornecedor"
        );

        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoService.buscarPorConta(10L)).thenReturn(List.of(credito, debito));

        byte[] pdf = service.gerarExtratoPdf(INICIO, FIM);

        assertThat(pdf).isNotNull().isNotEmpty();
        // Primeiros bytes de um PDF válido são sempre "%PDF"
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("gerarExtratoPdf: deve lançar exceção quando usuário não tem conta")
    void gerarExtratoPdf_deveLancarExcecaoQuandoSemConta() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of());

        assertThatThrownBy(() -> service.gerarExtratoPdf(INICIO, FIM))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada");
    }
}