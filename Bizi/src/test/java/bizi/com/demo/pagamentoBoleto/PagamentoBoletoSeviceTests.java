package bizi.com.demo.pagamentoBoleto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.usuario.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class PagamentoBoletoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaBancariaRepository contaRepository;

    @Mock
    private PagamentoBoletoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoBoletoService service;

    private ContaBancariaModel conta;
    private UsuarioModel usuario;
    private TransacaoModel transacao;
    private PagamentoBoletoModel pagamento;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setEmail("cliente@email.com");
        usuario.setNomeCompleto("Cliente Teste");

        conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setUsuario(usuario);
        conta.setSaldo(new BigDecimal("1000.00"));

        transacao = new TransacaoModel();
        transacao.setId(100L);
        transacao.setContaBancaria(conta);
        transacao.setValor(new BigDecimal("150.00"));

        pagamento = new PagamentoBoletoModel();
        pagamento.setId(1L);
        pagamento.setTransacao(transacao);
        pagamento.setCodigoBarras("34191.23456 78901.234567 89012.345678 1 12340000015000");
        pagamento.setNomeBeneficiario("Empresa Teste LTDA");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================
    //  realizarPagamento
    // =========================================================

    @Test
    @DisplayName("realizarPagamento: deve delegar corretamente para pagarBoleto")
    void realizarPagamento_deveDelegarParaPagarBoleto() {
        autenticarComo("cliente@email.com");

        PagamentoBoletoDto dto = new PagamentoBoletoDto(pagamento);
        dto.setCodigoBarras("34191.23456 78901.234567 89012.345678 1 12340000015000");
        dto.setValor(new BigDecimal("150.00"));
        dto.setNomeBeneficiario("Empresa Teste LTDA");

        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenReturn(transacao);
        when(pagamentoRepository.save(any(PagamentoBoletoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoBoletoModel resultado = service.realizarPagamento(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigoBarras()).isEqualTo(dto.getCodigoBarras());
        assertThat(resultado.getNomeBeneficiario()).isEqualTo(dto.getNomeBeneficiario());

        verify(contaRepository).save(conta);
        verify(transacaoRepository).save(any(TransacaoModel.class));
        verify(pagamentoRepository).save(any(PagamentoBoletoModel.class));
    }

    // =========================================================
    //  pagarBoleto
    // =========================================================

    @Test
    @DisplayName("pagarBoleto: deve debitar saldo da conta do usuário logado")
    void pagarBoleto_deveDebitarSaldoDaConta() {
        autenticarComo("cliente@email.com");

        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenReturn(transacao);
        when(pagamentoRepository.save(any(PagamentoBoletoModel.class))).thenReturn(pagamento);

        service.pagarBoleto("codigo-barras", new BigDecimal("300.00"), "Beneficiário");

        assertThat(conta.getSaldo()).isEqualByComparingTo("700.00");
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("pagarBoleto: deve salvar transação e pagamento após débito")
    void pagarBoleto_deveSalvarTransacaoEPagamento() {
        autenticarComo("cliente@email.com");

        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenReturn(transacao);
        when(pagamentoRepository.save(any(PagamentoBoletoModel.class))).thenReturn(pagamento);

        PagamentoBoletoModel resultado = service.pagarBoleto(
                "34191.23456", new BigDecimal("150.00"), "Empresa Teste LTDA");

        verify(transacaoRepository).save(any(TransacaoModel.class));
        verify(pagamentoRepository).save(any(PagamentoBoletoModel.class));
        assertThat(resultado.getNomeBeneficiario()).isEqualTo("Empresa Teste LTDA");
    }

    @Test
    @DisplayName("pagarBoleto: deve registrar transação com tipo PAGAMENTO_BOLETO")
    void pagarBoleto_deveRegistrarTransacaoComTipoCorreto() {
        autenticarComo("cliente@email.com");

        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> {
            TransacaoModel transacaoSalva = inv.getArgument(0);
            assertThat(transacaoSalva.getTipoTransacao().name()).isEqualTo("PAGAMENTO_BOLETO");
            return transacaoSalva;
        });
        when(pagamentoRepository.save(any(PagamentoBoletoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        service.pagarBoleto("codigo", new BigDecimal("100.00"), "Beneficiário");

        verify(transacaoRepository).save(any(TransacaoModel.class));
    }

    @Test
    @DisplayName("pagarBoleto: deve lançar exceção quando saldo insuficiente")
    void pagarBoleto_deveLancarExcecaoQuandoSaldoInsuficiente() {
        autenticarComo("cliente@email.com");

        when(contaRepository.findAll()).thenReturn(List.of(conta));

        assertThatThrownBy(() -> service.pagarBoleto("codigo", new BigDecimal("9999.00"), "Beneficiário"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("pagarBoleto: deve lançar exceção quando nenhuma conta vinculada ao e-mail do usuário")
    void pagarBoleto_deveLancarExcecaoQuandoContaNaoEncontrada() {
        autenticarComo("cliente@email.com");

        when(contaRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.pagarBoleto("codigo", new BigDecimal("100.00"), "Beneficiário"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada");

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("pagarBoleto: deve lançar exceção quando e-mail do usuário logado não bate com nenhuma conta")
    void pagarBoleto_deveLancarExcecaoQuandoEmailNaoCorresponde() {
        autenticarComo("cliente@email.com");

        UsuarioModel outroUsuario = new UsuarioModel();
        outroUsuario.setEmail("outro@email.com");

        ContaBancariaModel outraConta = new ContaBancariaModel();
        outraConta.setUsuario(outroUsuario);
        outraConta.setSaldo(new BigDecimal("500.00"));

        when(contaRepository.findAll()).thenReturn(List.of(outraConta));

        assertThatThrownBy(() -> service.pagarBoleto("codigo", new BigDecimal("100.00"), "Beneficiário"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada");
    }

    @Test
    @DisplayName("pagarBoleto: deve permitir pagamento com valor igual ao saldo exato")
    void pagarBoleto_devePermitirPagamentoComSaldoExato() {
        autenticarComo("cliente@email.com");

        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenReturn(transacao);
        when(pagamentoRepository.save(any(PagamentoBoletoModel.class))).thenReturn(pagamento);

        service.pagarBoleto("codigo", new BigDecimal("1000.00"), "Beneficiário");

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(contaRepository).save(conta);
    }

    // =========================================================
    //  buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar DTO quando pagamento encontrado")
    void buscarPorId_deveRetornarDtoQuandoEncontrado() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        PagamentoBoletoDto resultado = service.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigoBarras()).isEqualTo(pagamento.getCodigoBarras());
        assertThat(resultado.getNomeBeneficiario()).isEqualTo(pagamento.getNomeBeneficiario());
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando pagamento não encontrado")
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
        when(pagamentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    // =========================================================
    //  buscarPorConta
    // =========================================================

    @Test
    @DisplayName("buscarPorConta: deve retornar lista de DTOs da conta")
    void buscarPorConta_deveRetornarListaDeDtos() {
        when(pagamentoRepository.findByTransacao_ContaBancaria_Id(10L))
                .thenReturn(List.of(pagamento));

        List<PagamentoBoletoDto> resultado = service.buscarPorConta(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigoBarras()).isEqualTo(pagamento.getCodigoBarras());
    }

    @Test
    @DisplayName("buscarPorConta: deve retornar lista vazia quando conta não tem pagamentos")
    void buscarPorConta_deveRetornarListaVaziaQuandoSemPagamentos() {
        when(pagamentoRepository.findByTransacao_ContaBancaria_Id(10L))
                .thenReturn(List.of());

        List<PagamentoBoletoDto> resultado = service.buscarPorConta(10L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarPorConta: deve mapear todos os pagamentos quando conta tem múltiplos")
    void buscarPorConta_deveMappearTodosOsPagamentos() {
        PagamentoBoletoModel pagamento2 = new PagamentoBoletoModel();
        pagamento2.setId(2L);
        pagamento2.setTransacao(transacao);
        pagamento2.setCodigoBarras("outro-codigo");
        pagamento2.setNomeBeneficiario("Outra Empresa");

        when(pagamentoRepository.findByTransacao_ContaBancaria_Id(10L))
                .thenReturn(List.of(pagamento, pagamento2));

        List<PagamentoBoletoDto> resultado = service.buscarPorConta(10L);

        assertThat(resultado).hasSize(2);
        verify(pagamentoRepository, times(1)).findByTransacao_ContaBancaria_Id(10L);
    }

    private void autenticarComo(String email) {
        var authentication = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}