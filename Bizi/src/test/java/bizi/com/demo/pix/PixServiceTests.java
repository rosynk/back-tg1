package bizi.com.demo.pix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.chavePix.ChavePixModel;
import bizi.com.demo.chavePix.ChavePixRepository;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.security.SecurityUtil;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.transacao.TransacaoService;
import bizi.com.demo.usuario.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class PixServiceTest {

    @Mock private PixRepository pixRepository;
    @Mock private ChavePixRepository chavePixRepository;
    @Mock private ContaBancariaService contaService;
    @Mock private ContaBancariaRepository contaRepository;
    @Mock private TransacaoRepository transacaoRepository;
    @Mock private TransacaoService transacaoService;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks
    private PixService pixService;

    private UsuarioModel usuarioOrigem;
    private UsuarioModel usuarioDestino;
    private ContaBancariaModel contaOrigem;
    private ContaBancariaModel contaDestino;
    private ChavePixModel chavePix;
    private TransacaoModel transacaoSaida;

    @BeforeEach
    void setUp() {
        usuarioOrigem = new UsuarioModel();
        usuarioOrigem.setId(1L);
        usuarioOrigem.setCpf("11111111111");
        usuarioOrigem.setNomeCompleto("João Origem");
        usuarioOrigem.setEmail("joao@email.com");

        usuarioDestino = new UsuarioModel();
        usuarioDestino.setId(2L);
        usuarioDestino.setCpf("22222222222");
        usuarioDestino.setNomeCompleto("Maria Destino");
        usuarioDestino.setEmail("maria@email.com");

        contaOrigem = new ContaBancariaModel();
        contaOrigem.setId(10L);
        contaOrigem.setUsuario(usuarioOrigem);
        contaOrigem.setSaldo(new BigDecimal("1000.00"));
        contaOrigem.setNumeroConta("111111");
        contaOrigem.setNumeroAgencia("0001");

        contaDestino = new ContaBancariaModel();
        contaDestino.setId(20L);
        contaDestino.setUsuario(usuarioDestino);
        contaDestino.setSaldo(new BigDecimal("500.00"));

        chavePix = new ChavePixModel();
        chavePix.setValor("maria@email.com");
        chavePix.setConta(contaDestino);

        transacaoSaida = new TransacaoModel();
        transacaoSaida.setId(100L);
        transacaoSaida.setContaBancaria(contaOrigem);
        transacaoSaida.setValor(new BigDecimal("200.00"));
    }

    // =========================================================
    //  buscarInformacoesResumo
    // =========================================================

    @Test
    @DisplayName("buscarInformacoesResumo: deve retornar saldo, numeroConta e extrato da conta do usuário logado")
    void buscarInformacoesResumo_deveRetornarDadosDaConta() {
        contaOrigem.setTransacoes(List.of());
        when(securityUtil.getUsuarioLogado()).thenReturn(usuarioOrigem);
        when(contaRepository.findByUsuarioId(1L)).thenReturn(List.of(contaOrigem));

        Map<String, Object> resumo = pixService.buscarInformacoesResumo();

        assertThat(resumo.get("saldo")).isEqualTo(new BigDecimal("1000.00"));
        assertThat(resumo.get("numeroConta")).isEqualTo("111111");
        assertThat(resumo.get("numeroAgencia")).isEqualTo("0001");
        assertThat(resumo.get("extrato")).isNotNull();
    }

    @Test
    @DisplayName("buscarInformacoesResumo: deve retornar valores zerados quando usuário não tem conta")
    void buscarInformacoesResumo_deveRetornarValoresZeradosQuandoSemConta() {
        when(securityUtil.getUsuarioLogado()).thenReturn(usuarioOrigem);
        when(contaRepository.findByUsuarioId(1L)).thenReturn(List.of());

        Map<String, Object> resumo = pixService.buscarInformacoesResumo();

        assertThat(resumo.get("saldo")).isEqualTo(BigDecimal.ZERO);
        assertThat(resumo.get("numeroConta")).isEqualTo("Pendente");
        assertThat(resumo.get("agencia")).isEqualTo("0000");
        assertThat((List<?>) resumo.get("extrato")).isEmpty();
    }

    // =========================================================
    //  realizarPix
    // =========================================================

    @Test
    @DisplayName("realizarPix: deve debitar origem e creditar destino corretamente")
    void realizarPix_deveDebitarOrigemECreditarDestino() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("200.00"));
        dto.setMensagem("Pagamento teste");

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(chavePixRepository.findByValor("maria@email.com")).thenReturn(Optional.of(chavePix));
        when(transacaoService.criarTransacao(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(transacaoSaida);
        when(pixRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pixService.realizarPix(dto);

        assertThat(contaOrigem.getSaldo()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(contaDestino.getSaldo()).isEqualByComparingTo(new BigDecimal("700.00"));
        verify(contaRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("realizarPix: deve registrar transação de SAÍDA na conta origem e ENTRADA na conta destino")
    void realizarPix_deveRegistrarTransacoesDeSaidaEEntrada() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("200.00"));

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(chavePixRepository.findByValor("maria@email.com")).thenReturn(Optional.of(chavePix));
        when(transacaoService.criarTransacao(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(transacaoSaida);
        when(pixRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pixService.realizarPix(dto);

        verify(transacaoService).criarTransacao(
                eq(contaOrigem), eq(TipoTransacao.PIX_SAIDA),
                any(), any(), any(), any(), any());
        verify(transacaoService).criarTransacao(
                eq(contaDestino), eq(TipoTransacao.PIX_ENTRADA),
                any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("realizarPix: deve salvar PixModel com chave e mensagem corretas")
    void realizarPix_deveSalvarPixComDadosCorretos() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("100.00"));
        dto.setMensagem("Aluguel");

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(chavePixRepository.findByValor("maria@email.com")).thenReturn(Optional.of(chavePix));
        when(transacaoService.criarTransacao(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(transacaoSaida);
        when(pixRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PixModel resultado = pixService.realizarPix(dto);

        assertThat(resultado.getChavePixDestino()).isEqualTo("maria@email.com");
        assertThat(resultado.getMensagem()).isEqualTo("Aluguel");
        assertThat(resultado.getTransacao()).isEqualTo(transacaoSaida);
        verify(pixRepository).save(any());
    }

    @Test
    @DisplayName("realizarPix: deve lançar exceção quando saldo insuficiente")
    void realizarPix_deveLancarExcecaoQuandoSaldoInsuficiente() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("9999.00"));

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));

        assertThatThrownBy(() -> pixService.realizarPix(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(contaRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarPix: deve lançar exceção quando chave Pix não encontrada")
    void realizarPix_deveLancarExcecaoQuandoChaveNaoEncontrada() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("inexistente@email.com");
        dto.setValor(new BigDecimal("100.00"));

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(chavePixRepository.findByValor("inexistente@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pixService.realizarPix(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Chave Pix não encontrada");

        verify(contaRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarPix: deve lançar exceção quando origem e destino são a mesma conta")
    void realizarPix_deveLancarExcecaoQuandoPixParaProprioUsuario() {
        // Chave aponta para a própria conta de origem
        ChavePixModel chavePropriaContaOrigem = new ChavePixModel();
        chavePropriaContaOrigem.setValor("joao@email.com");
        chavePropriaContaOrigem.setConta(contaOrigem);

        PixDto dto = new PixDto();
        dto.setChavePixDestino("joao@email.com");
        dto.setValor(new BigDecimal("100.00"));

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(chavePixRepository.findByValor("joao@email.com")).thenReturn(Optional.of(chavePropriaContaOrigem));

        assertThatThrownBy(() -> pixService.realizarPix(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("própria conta");

        verify(contaRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarPix: deve permitir Pix com valor igual ao saldo exato")
    void realizarPix_devePermitirPixComSaldoExato() {
        PixDto dto = new PixDto();
        dto.setChavePixDestino("maria@email.com");
        dto.setValor(new BigDecimal("1000.00"));

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(chavePixRepository.findByValor("maria@email.com")).thenReturn(Optional.of(chavePix));
        when(transacaoService.criarTransacao(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(transacaoSaida);
        when(pixRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pixService.realizarPix(dto);

        assertThat(contaOrigem.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(contaDestino.getSaldo()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    // =========================================================
    //  listarPixDoUsuarioLogado
    // =========================================================

    @Test
    @DisplayName("listarPixDoUsuarioLogado: deve retornar lista de Pix da conta do usuário logado")
    void listarPixDoUsuarioLogado_deveRetornarListaDePix() {
        PixModel pix = new PixModel();
        pix.setChavePixDestino("maria@email.com");

        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(pixRepository.findByTransacao_ContaBancaria(contaOrigem)).thenReturn(List.of(pix));

        List<PixModel> resultado = pixService.listarPixDoUsuarioLogado();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getChavePixDestino()).isEqualTo("maria@email.com");
    }

    @Test
    @DisplayName("listarPixDoUsuarioLogado: deve retornar lista vazia quando usuário não tem histórico")
    void listarPixDoUsuarioLogado_deveRetornarListaVaziaQuandoSemHistorico() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(contaOrigem));
        when(pixRepository.findByTransacao_ContaBancaria(contaOrigem)).thenReturn(List.of());

        List<PixModel> resultado = pixService.listarPixDoUsuarioLogado();

        assertThat(resultado).isEmpty();
    }
}