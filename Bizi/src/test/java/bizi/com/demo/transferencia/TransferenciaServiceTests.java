package bizi.com.demo.transferencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.usuario.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private ContaBancariaRepository contaBancariaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private TransferenciaService service;

    private UsuarioModel usuarioOrigem;
    private UsuarioModel usuarioDestino;
    private ContaBancariaModel contaOrigem;
    private ContaBancariaModel contaDestino;

    @BeforeEach
    void setUp() {
        usuarioOrigem = new UsuarioModel();
        usuarioOrigem.setCpf("12345678900");
        usuarioOrigem.setNomeCompleto("Joao Origem");

        usuarioDestino = new UsuarioModel();
        usuarioDestino.setCpf("98765432100");
        usuarioDestino.setNomeCompleto("Maria Destino");

        contaOrigem = new ContaBancariaModel();
        contaOrigem.setId(1L);
        contaOrigem.setUsuario(usuarioOrigem);
        contaOrigem.setSaldo(new BigDecimal("1000.00"));
        contaOrigem.setStatusConta(true);

        contaDestino = new ContaBancariaModel();
        contaDestino.setId(2L);
        contaDestino.setUsuario(usuarioDestino);
        contaDestino.setSaldo(new BigDecimal("500.00"));
        contaDestino.setStatusConta(true);

        autenticarCliente("12345678900");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("realizarTransferencia: deve transferir valor, criar transacoes e retornar recibo")
    void realizarTransferencia_deveTransferirCriarTransacoesERetornarRecibo() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setContaOrigem(1L);
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(new BigDecimal("100.00"));
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "22222-2"))
                .thenReturn(Optional.of(contaDestino));
        when(transacaoRepository.findByContaBancariaIdAndDataHoraAfter(any(), any()))
                .thenReturn(List.of());
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> {
            TransacaoModel transacao = inv.getArgument(0);
            transacao.setDataHora(LocalDateTime.now());
            return transacao;
        });
        when(transferenciaRepository.save(any(TransferenciaModel.class))).thenAnswer(inv -> {
            TransferenciaModel transferencia = inv.getArgument(0);
            transferencia.setId(10L);
            return transferencia;
        });

        TransferenciaDto recibo = service.realizarTransferencia(dto);

        assertThat(contaOrigem.getSaldo()).isEqualByComparingTo("900.00");
        assertThat(contaDestino.getSaldo()).isEqualByComparingTo("600.00");
        assertThat(recibo.getIdTransferencia()).isEqualTo(10L);
        assertThat(recibo.getContaOrigem()).isEqualTo(1L);
        assertThat(recibo.getNomeOrigem()).isEqualTo("Joao Origem");
        assertThat(recibo.getNomeDestino()).isEqualTo("Maria Destino");
        assertThat(recibo.getStatus()).isEqualTo("CONCLUIDA");
        assertThat(recibo.getMensagem()).isEqualTo("Transferência DOC realizada com sucesso.");

        verify(contaBancariaRepository).save(contaOrigem);
        verify(contaBancariaRepository).save(contaDestino);
        verify(transacaoRepository, times(2)).save(any(TransacaoModel.class));
        verify(transferenciaRepository).save(any(TransferenciaModel.class));
    }

    @Test
    @DisplayName("realizarTransferencia: deve lançar exceção quando conta destino não existe")
    void realizarTransferencia_deveLancarQuandoContaDestinoNaoExiste() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("99999-9");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "99999-9"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta de destino não encontrada");

        verify(transferenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarTransferencia: deve lançar exceção ao transferir para si mesmo")
    void realizarTransferencia_deveLancarQuandoMesmaConta() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setContaOrigem(1L);
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("11111-1");
        dto.setValor(new BigDecimal("50.00"));
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "11111-1"))
                .thenReturn(Optional.of(contaOrigem));

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Não é possível transferir para si mesmo");
    }

    @Test
    @DisplayName("realizarTransferencia: deve lançar exceção quando valor é inválido")
    void realizarTransferencia_deveLancarQuandoValorInvalido() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(BigDecimal.ZERO);
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "22222-2"))
                .thenReturn(Optional.of(contaDestino));

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Valor inválido");
    }

    @Test
    @DisplayName("realizarTransferencia: deve lançar exceção quando saldo é insuficiente")
    void realizarTransferencia_deveLancarQuandoSaldoInsuficiente() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(new BigDecimal("2000.00"));
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "22222-2"))
                .thenReturn(Optional.of(contaDestino));

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    @DisplayName("realizarTransferencia: deve lançar exceção quando conta está inativa")
    void realizarTransferencia_deveLancarQuandoContaInativa() {
        contaDestino.setStatusConta(false);

        TransferenciaDto dto = new TransferenciaDto();
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(new BigDecimal("100.00"));
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "22222-2"))
                .thenReturn(Optional.of(contaDestino));

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Uma das contas está inativa");
    }

    @Test
    @DisplayName("realizarTransferencia: deve lançar exceção quando limite diário é excedido")
    void realizarTransferencia_deveLancarQuandoLimiteDiarioExcedido() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(new BigDecimal("200.00"));
        dto.setTipoTransferencia("DOC");

        contaOrigem.setSaldo(new BigDecimal("20000.00"));

        TransacaoModel enviadaHoje = new TransacaoModel();
        enviadaHoje.setTipoTransacao(TipoTransacao.TRANSFERENCIA_ENVIADA);
        enviadaHoje.setValor(new BigDecimal("9900.00"));

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "22222-2"))
                .thenReturn(Optional.of(contaDestino));
        when(transacaoRepository.findByContaBancariaIdAndDataHoraAfter(any(), any()))
                .thenReturn(List.of(enviadaHoje));

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Limite diário");
    }

    @Test
    @DisplayName("realizarTransferencia: admin deve buscar conta origem pelo ID informado")
    void realizarTransferencia_adminDeveBuscarContaOrigemPorId() {
        autenticarAdmin();

        TransferenciaDto dto = new TransferenciaDto();
        dto.setContaOrigem(1L);
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(new BigDecimal("100.00"));
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "22222-2"))
                .thenReturn(Optional.of(contaDestino));
        when(transacaoRepository.findByContaBancariaIdAndDataHoraAfter(any(), any()))
                .thenReturn(List.of());
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> {
            TransacaoModel transacao = inv.getArgument(0);
            transacao.setDataHora(LocalDateTime.now());
            return transacao;
        });
        when(transferenciaRepository.save(any(TransferenciaModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferenciaDto recibo = service.realizarTransferencia(dto);

        assertThat(recibo.getContaOrigem()).isEqualTo(1L);
        verify(contaBancariaRepository).findById(1L);
    }

    @Test
    @DisplayName("realizarTransferencia: admin deve lançar exceção quando conta origem não existe")
    void realizarTransferencia_adminDeveLancarQuandoContaOrigemNaoExiste() {
        autenticarAdmin();

        TransferenciaDto dto = new TransferenciaDto();
        dto.setContaOrigem(99L);

        when(contaBancariaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta id 99 não encontrada");
    }

    @Test
    @DisplayName("realizarTransferencia: cliente deve lançar exceção quando não possui conta")
    void realizarTransferencia_clienteDeveLancarQuandoNaoPossuiConta() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("22222-2");
        dto.setValor(new BigDecimal("100.00"));
        dto.setTipoTransferencia("DOC");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizarTransferencia(dto))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta não localizada");
    }

    @Test
    @DisplayName("buscarTodasDaConta: deve retornar transferências quando usuário tem acesso")
    void buscarTodasDaConta_deveRetornarQuandoUsuarioTemAcesso() {
        List<TransferenciaModel> lista = List.of(new TransferenciaModel());

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(transferenciaRepository.findByContaOrigemOrDestino(1L)).thenReturn(lista);

        List<TransferenciaModel> resultado = service.buscarTodasDaConta(1L);

        assertThat(resultado).isEqualTo(lista);
    }

    @Test
    @DisplayName("buscarTodasDaConta: deve negar acesso para conta de terceiro")
    void buscarTodasDaConta_deveNegarContaDeTerceiro() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));

        assertThatThrownBy(() -> service.buscarTodasDaConta(2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão");
    }

    @Test
    @DisplayName("buscarTodasDaConta: admin deve acessar qualquer conta")
    void buscarTodasDaConta_adminDeveAcessarQualquerConta() {
        autenticarAdmin();

        List<TransferenciaModel> lista = List.of(new TransferenciaModel());
        when(transferenciaRepository.findByContaOrigemOrDestino(2L)).thenReturn(lista);

        List<TransferenciaModel> resultado = service.buscarTodasDaConta(2L);

        assertThat(resultado).isEqualTo(lista);
    }

    @Test
    @DisplayName("buscarTodasDaConta: deve lançar AccessDenied quando usuário logado não tem conta")
    void buscarTodasDaConta_deveLancarQuandoUsuarioLogadoNaoTemConta() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarTodasDaConta(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Usuário não possui conta vinculada");
    }

    @Test
    @DisplayName("buscarPorId: deve retornar transferência quando encontrada e autorizada")
    void buscarPorId_deveRetornarQuandoEncontradaEAutorizada() {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaOrigem);

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacao);

        when(transferenciaRepository.findById(1L)).thenReturn(Optional.of(transferencia));
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));

        TransferenciaModel resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(transferencia);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando ID não existe")
    void buscarPorId_deveLancarQuandoNaoExiste() {
        when(transferenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transferência não encontrada");
    }

    @Test
    @DisplayName("buscarPorId: admin deve acessar transferência de qualquer conta")
    void buscarPorId_adminDeveAcessarTransferenciaDeQualquerConta() {
        autenticarAdmin();

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaOrigem);

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacao);

        when(transferenciaRepository.findById(1L)).thenReturn(Optional.of(transferencia));

        TransferenciaModel resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(transferencia);
    }

    @Test
    @DisplayName("gerarCsvExtrato: deve gerar CSV com envio negativo")
    void gerarCsvExtrato_deveGerarCsvComEnvioNegativo() {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaOrigem);
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setDataHora(LocalDateTime.of(2026, 5, 19, 10, 0));

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setId(1L);
        transferencia.setTransacao(transacao);
        transferencia.setTipoTransferencia("DOC");
        transferencia.setNomeContraparte("Maria Destino");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(transferenciaRepository.findByContaOrigemOrDestino(1L)).thenReturn(List.of(transferencia));

        String csv = new String(service.gerarCsvExtrato(1L), StandardCharsets.UTF_8);

        assertThat(csv).contains("ID;Data;Tipo Transferencia;Direcao;Favorecido/Pagador;Valor;Status");
        assertThat(csv).contains("1;2026-05-19T10:00;DOC;ENVIO;Maria Destino;-100.00;CONCLUIDA");
    }

    @Test
    @DisplayName("gerarCsvExtrato: deve gerar CSV com recebimento positivo")
    void gerarCsvExtrato_deveGerarCsvComRecebimentoPositivo() {
        autenticarCliente("98765432100");

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaOrigem);
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setDataHora(LocalDateTime.of(2026, 5, 19, 10, 0));

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setId(1L);
        transferencia.setTransacao(transacao);
        transferencia.setContaDestino(2L);
        transferencia.setTipoTransferencia("DOC");
        transferencia.setNomeContraparte("Joao Origem");

        when(contaBancariaRepository.findByUsuarioCpf("98765432100")).thenReturn(Optional.of(contaDestino));
        when(transferenciaRepository.findByContaOrigemOrDestino(2L)).thenReturn(List.of(transferencia));

        String csv = new String(service.gerarCsvExtrato(2L), StandardCharsets.UTF_8);

        assertThat(csv).contains("RECEBIMENTO");
        assertThat(csv).contains("100.00");
    }

    @Test
    @DisplayName("gerarCsvExtrato: deve gerar apenas cabeçalho quando não há transferências")
    void gerarCsvExtrato_deveGerarApenasCabecalhoQuandoSemTransferencias() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(transferenciaRepository.findByContaOrigemOrDestino(1L)).thenReturn(List.of());

        String csv = new String(service.gerarCsvExtrato(1L), StandardCharsets.UTF_8);

        assertThat(csv).isEqualTo("ID;Data;Tipo Transferencia;Direcao;Favorecido/Pagador;Valor;Status\n");
    }

    @Test
    @DisplayName("gerarPdfExtrato: deve gerar bytes de PDF")
    void gerarPdfExtrato_deveGerarBytesPdf() {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaOrigem);
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setDataHora(LocalDateTime.now());

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacao);
        transferencia.setTipoTransferencia("DOC");
        transferencia.setNomeContraparte("Maria Destino");

        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(transferenciaRepository.findByContaOrigemOrDestino(1L)).thenReturn(List.of(transferencia));

        byte[] pdf = service.gerarPdfExtrato(1L);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("gerarPdfExtrato: deve gerar PDF mesmo sem transferências")
    void gerarPdfExtrato_deveGerarPdfMesmoSemTransferencias() {
        when(contaBancariaRepository.findByUsuarioCpf("12345678900")).thenReturn(Optional.of(contaOrigem));
        when(transferenciaRepository.findByContaOrigemOrDestino(1L)).thenReturn(List.of());

        byte[] pdf = service.gerarPdfExtrato(1L);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("estornarTransferencia: deve estornar quando usuário é admin")
    void estornarTransferencia_deveEstornarQuandoAdmin() {
        autenticarAdmin();

        TransacaoModel transacaoOriginal = new TransacaoModel();
        transacaoOriginal.setContaBancaria(contaOrigem);
        transacaoOriginal.setValor(new BigDecimal("100.00"));

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setId(1L);
        transferencia.setTransacao(transacaoOriginal);
        transferencia.setContaDestino(2L);

        when(transferenciaRepository.findById(1L)).thenReturn(Optional.of(transferencia));
        when(contaBancariaRepository.findById(2L)).thenReturn(Optional.of(contaDestino));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        service.estornarTransferencia(1L);

        assertThat(contaOrigem.getSaldo()).isEqualByComparingTo("1100.00");
        assertThat(contaDestino.getSaldo()).isEqualByComparingTo("400.00");

        verify(contaBancariaRepository).save(contaOrigem);
        verify(contaBancariaRepository).save(contaDestino);
        verify(transferenciaRepository).delete(transferencia);
    }

    @Test
    @DisplayName("estornarTransferencia: deve negar quando usuário não é admin")
    void estornarTransferencia_deveNegarQuandoNaoAdmin() {
        assertThatThrownBy(() -> service.estornarTransferencia(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Apenas administradores");

        verify(transferenciaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("estornarTransferencia: deve lançar exceção quando transferência não existe")
    void estornarTransferencia_deveLancarQuandoTransferenciaNaoExiste() {
        autenticarAdmin();

        when(transferenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.estornarTransferencia(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transferência não encontrada");

        verify(transferenciaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("estornarTransferencia: deve lançar exceção quando conta destino não existe")
    void estornarTransferencia_deveLancarQuandoContaDestinoNaoExiste() {
        autenticarAdmin();

        TransacaoModel transacaoOriginal = new TransacaoModel();
        transacaoOriginal.setContaBancaria(contaOrigem);
        transacaoOriginal.setValor(new BigDecimal("100.00"));

        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setId(1L);
        transferencia.setTransacao(transacaoOriginal);
        transferencia.setContaDestino(99L);

        when(transferenciaRepository.findById(1L)).thenReturn(Optional.of(transferencia));
        when(contaBancariaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.estornarTransferencia(1L))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta de destino do estorno não encontrada");

        verify(transferenciaRepository, never()).delete(any());
    }

    private void autenticarCliente(String cpf) {
        var auth = new UsernamePasswordAuthenticationToken(
                cpf,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void autenticarAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}