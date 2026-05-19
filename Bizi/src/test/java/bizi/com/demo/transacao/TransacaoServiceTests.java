package bizi.com.demo.transacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ContaBancariaRepository contaBancariaRepository;

    @InjectMocks
    private TransacaoService service;

    private UsuarioModel usuarioLogado;
    private UsuarioModel outroUsuario;
    private ContaBancariaModel conta;

    @BeforeEach
    void setUp() {
        usuarioLogado = new UsuarioModel();
        usuarioLogado.setCpf("12345678900");
        usuarioLogado.setNomeCompleto("Cliente Logado");

        outroUsuario = new UsuarioModel();
        outroUsuario.setCpf("98765432100");
        outroUsuario.setNomeCompleto("Maria Silva");

        conta = new ContaBancariaModel();
        conta.setId(1L);
        conta.setUsuario(usuarioLogado);
        conta.setSaldo(new BigDecimal("100.00"));

        autenticarComoCliente(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================
    //  criarTransacao - persistencia direta
    // =========================================================

    @Test
    @DisplayName("criarTransacao overload: deve preencher campos e salvar")
    void criarTransacaoOverload_devePreencherCamposESalvar() {
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.criarTransacao(
                conta,
                TipoTransacao.PIX_SAIDA,
                new BigDecimal("25.50"),
                "12345678900",
                "98765432100",
                "Maria Silva",
                "Pix enviada para Maria Silva");

        assertThat(resultado.getContaBancaria()).isEqualTo(conta);
        assertThat(resultado.getTipoTransacao()).isEqualTo(TipoTransacao.PIX_SAIDA);
        assertThat(resultado.getValor()).isEqualByComparingTo("25.50");
        assertThat(resultado.getCpfOrigem()).isEqualTo("12345678900");
        assertThat(resultado.getCpfDestino()).isEqualTo("98765432100");
        assertThat(resultado.getNomeContraparte()).isEqualTo("Maria Silva");
        assertThat(resultado.getDetalhe()).isEqualTo("Pix enviada para Maria Silva");
        assertThat(resultado.getDataHora()).isNotNull();
        verify(transacaoRepository).save(any(TransacaoModel.class));
    }

    @Test
    @DisplayName("criarTransacao dto: deve buscar favorecido, formatar detalhe e salvar")
    void criarTransacaoDto_deveBuscarFavorecidoFormatarDetalheESalvar() {
        TransacaoDto dto = org.mockito.Mockito.mock(TransacaoDto.class);
        when(dto.getIdConta()).thenReturn(1L);
        when(dto.getCpfDestino()).thenReturn("98765432100");
        when(dto.getTipoTransacao()).thenReturn(TipoTransacao.PIX_SAIDA);
        when(dto.getValor()).thenReturn(new BigDecimal("50.00"));

        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(usuarioRepository.findByCpf("98765432100")).thenReturn(Optional.of(outroUsuario));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.criarTransacao(dto);

        assertThat(resultado.getContaBancaria()).isEqualTo(conta);
        assertThat(resultado.getCpfOrigem()).isEqualTo("12345678900");
        assertThat(resultado.getCpfDestino()).isEqualTo("98765432100");
        assertThat(resultado.getNomeContraparte()).isEqualTo("Maria Silva");
        assertThat(resultado.getDetalhe()).isEqualTo("Pix enviada para Maria Silva");
    }

    @Test
    @DisplayName("criarTransacao dto: deve usar Conta nao localizada quando CPF destino nao existe")
    void criarTransacaoDto_deveUsarContaNaoLocalizadaQuandoCpfDestinoNaoExiste() {
        TransacaoDto dto = org.mockito.Mockito.mock(TransacaoDto.class);
        when(dto.getIdConta()).thenReturn(1L);
        when(dto.getCpfDestino()).thenReturn("00000000000");
        when(dto.getTipoTransacao()).thenReturn(TipoTransacao.PIX_SAIDA);
        when(dto.getValor()).thenReturn(new BigDecimal("10.00"));

        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(usuarioRepository.findByCpf("00000000000")).thenReturn(Optional.empty());
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.criarTransacao(dto);

        assertThat(resultado.getNomeContraparte()).isEqualTo("Conta não localizada");
        assertThat(resultado.getDetalhe()).isEqualTo("Pix enviada para Conta não localizada");
    }

    @Test
    @DisplayName("criarTransacao dto: deve lancar excecao quando conta nao existe")
    void criarTransacaoDto_deveLancarExcecaoQuandoContaNaoExiste() {
        TransacaoDto dto = org.mockito.Mockito.mock(TransacaoDto.class);
        when(dto.getIdConta()).thenReturn(99L);
        when(contaBancariaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarTransacao(dto))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta não encontrada");

        verify(transacaoRepository, never()).save(any());
    }

    // =========================================================
    //  saque e deposito
    // =========================================================

    @Test
    @DisplayName("realizarSaque: deve debitar saldo e salvar transacao")
    void realizarSaque_deveDebitarSaldoESalvarTransacao() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.realizarSaque(new BigDecimal("30.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("70.00");
        assertThat(resultado.getTipoTransacao()).isEqualTo(TipoTransacao.SAQUE);
        assertThat(resultado.getNomeContraparte()).isEqualTo("Retirada de Recurso");
        assertThat(resultado.getCpfOrigem()).isEqualTo("12345678900");
        verify(contaBancariaRepository).save(conta);
    }

    @Test
    @DisplayName("realizarSaque: deve lancar excecao quando saldo insuficiente")
    void realizarSaque_deveLancarExcecaoQuandoSaldoInsuficiente() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.realizarSaque(new BigDecimal("150.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(contaBancariaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarDeposito: deve creditar saldo e salvar transacao")
    void realizarDeposito_deveCreditarSaldoESalvarTransacao() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        service.realizarDeposito(new BigDecimal("40.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("140.00");
        verify(contaBancariaRepository).save(conta);
        verify(transacaoRepository).save(any(TransacaoModel.class));
    }

    // =========================================================
    //  extrato e filtros
    // =========================================================

    @Test
    @DisplayName("listarExtratoCompleto: deve buscar por CPF logado")
    void listarExtratoCompleto_deveBuscarPorCpfLogado() {
        List<TransacaoModel> transacoes = List.of(new TransacaoModel());
        when(transacaoRepository.findByCpfParaExtrato("12345678900")).thenReturn(transacoes);

        List<TransacaoModel> resultado = service.listarExtratoCompleto();

        assertThat(resultado).isEqualTo(transacoes);
    }

    @Test
    @DisplayName("buscarPorPeriodo: deve filtrar transacoes entre inicio e fim")
    void buscarPorPeriodo_deveFiltrarEntreInicioEFim() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 31, 23, 59);

        TransacaoModel dentro = transacaoComDataETipo(LocalDateTime.of(2026, 1, 10, 12, 0), TipoTransacao.PIX_SAIDA);
        TransacaoModel antes = transacaoComDataETipo(LocalDateTime.of(2025, 12, 31, 23, 59), TipoTransacao.PIX_SAIDA);
        TransacaoModel depois = transacaoComDataETipo(LocalDateTime.of(2026, 2, 1, 0, 0), TipoTransacao.PIX_SAIDA);
        when(transacaoRepository.findByCpfParaExtrato("12345678900"))
                .thenReturn(List.of(dentro, antes, depois));

        List<TransacaoModel> resultado = service.buscarPorPeriodo(inicio, fim);

        assertThat(resultado).containsExactly(dentro);
    }

    @Test
    @DisplayName("buscarPorTipo: deve filtrar transacoes pelo tipo")
    void buscarPorTipo_deveFiltrarPeloTipo() {
        TransacaoModel pix = transacaoComDataETipo(LocalDateTime.now(), TipoTransacao.PIX_SAIDA);
        TransacaoModel saque = transacaoComDataETipo(LocalDateTime.now(), TipoTransacao.SAQUE);
        when(transacaoRepository.findByCpfParaExtrato("12345678900")).thenReturn(List.of(pix, saque));

        List<TransacaoModel> resultado = service.buscarPorTipo(TipoTransacao.PIX_SAIDA);

        assertThat(resultado).containsExactly(pix);
    }

    // =========================================================
    //  buscarPorId e posse da conta
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar transacao quando conta pertence ao usuario")
    void buscarPorId_deveRetornarQuandoContaPertenceAoUsuario() {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        TransacaoModel resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(transacao);
    }

    @Test
    @DisplayName("buscarPorId: deve permitir admin mesmo que conta seja de outro usuario")
    void buscarPorId_devePermitirAdmin() {
        autenticarComoAdmin(usuarioLogado);
        ContaBancariaModel contaDeOutroUsuario = new ContaBancariaModel();
        contaDeOutroUsuario.setUsuario(outroUsuario);

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaDeOutroUsuario);
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        TransacaoModel resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(transacao);
    }

    @Test
    @DisplayName("buscarPorId: deve negar acesso quando conta pertence a outro CPF")
    void buscarPorId_deveNegarQuandoContaDeOutroCpf() {
        ContaBancariaModel contaDeOutroUsuario = new ContaBancariaModel();
        contaDeOutroUsuario.setUsuario(outroUsuario);

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaDeOutroUsuario);
        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        assertThatThrownBy(() -> service.buscarPorId(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("CPF do titular não confere");
    }

    // =========================================================
    //  gestao admin
    // =========================================================

    @Test
    @DisplayName("listarTodas: deve retornar todas quando usuario e admin")
    void listarTodas_deveRetornarTodasQuandoAdmin() {
        autenticarComoAdmin(usuarioLogado);
        List<TransacaoModel> transacoes = List.of(new TransacaoModel(), new TransacaoModel());
        when(transacaoRepository.findAll()).thenReturn(transacoes);

        List<TransacaoModel> resultado = service.listarTodas();

        assertThat(resultado).isEqualTo(transacoes);
    }

    @Test
    @DisplayName("listarTodas: deve negar acesso quando usuario nao e admin")
    void listarTodas_deveNegarQuandoNaoAdmin() {
        assertThatThrownBy(() -> service.listarTodas())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    @DisplayName("deletarTransacao: deve deletar quando admin e ID existe")
    void deletarTransacao_deveDeletarQuandoAdminEIdExiste() {
        autenticarComoAdmin(usuarioLogado);
        when(transacaoRepository.existsById(1L)).thenReturn(true);

        service.deletarTransacao(1L);

        verify(transacaoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletarTransacao: deve negar acesso quando usuario nao e admin")
    void deletarTransacao_deveNegarQuandoNaoAdmin() {
        assertThatThrownBy(() -> service.deletarTransacao(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Somente administradores");

        verify(transacaoRepository, never()).deleteById(1L);
    }

    // =========================================================
    //  buscarPorConta
    // =========================================================

    @Test
    @DisplayName("buscarPorConta: deve validar posse e retornar historico ordenado")
    void buscarPorConta_deveValidarPosseERetornarHistorico() {
        List<TransacaoModel> historico = List.of(new TransacaoModel());
        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.findByContaBancariaIdOrderByDataHoraDesc(1L)).thenReturn(historico);

        List<TransacaoModel> resultado = service.buscarPorConta(1L);

        assertThat(resultado).isEqualTo(historico);
    }

    @Test
    @DisplayName("buscarPorConta: deve lancar excecao quando conta nao existe")
    void buscarPorConta_deveLancarQuandoContaNaoExiste() {
        when(contaBancariaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorConta(99L))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta bancária 99 não encontrada");
    }

    @Test
    @DisplayName("buscarPorConta: deve negar acesso quando conta pertence a outro CPF")
    void buscarPorConta_deveNegarQuandoContaDeOutroCpf() {
        ContaBancariaModel contaDeOutroUsuario = new ContaBancariaModel();
        contaDeOutroUsuario.setUsuario(outroUsuario);
        when(contaBancariaRepository.findById(2L)).thenReturn(Optional.of(contaDeOutroUsuario));

        assertThatThrownBy(() -> service.buscarPorConta(2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("CPF do titular não confere");
    }

    private void autenticarComoCliente(UsuarioModel usuario) {
        autenticar(usuario, "ROLE_CLIENTE");
    }

    private void autenticarComoAdmin(UsuarioModel usuario) {
        autenticar(usuario, "ROLE_ADMIN");
    }

    private void autenticar(UsuarioModel usuario, String authority) {
        var authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private TransacaoModel transacaoComDataETipo(LocalDateTime dataHora, TipoTransacao tipo) {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setDataHora(dataHora);
        transacao.setTipoTransacao(tipo);
        return transacao;
    }
}
