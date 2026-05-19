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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.usuario.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class PagamentoBoletoServiceTest {

    @Mock private TransacaoRepository transacaoRepository;
    @Mock private ContaBancariaRepository contaRepository;
    @Mock private PagamentoBoletoRepository pagamentoRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

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

        // Configura o SecurityContextHolder para todos os testes que precisam de usuário logado
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("cliente@email.com");
        SecurityContextHolder.setContext(securityContext);
    }

    // =========================================================
    //  realizarPagamento (via DTO)
    // =========================================================

    @Test
    @DisplayName("realizarPagamento: deve delegar corretamente para pagarBoleto")
    void realizarPagamento_deveDelegarParaPagarBoleto() {
        PagamentoBoletoDto dto = new PagamentoBoletoDto(pagamento);
        dto.setCodigoBarras("34191.23456 78901.234567 89012.345678 1 12340000015000");
        dto.setValor(new BigDecimal("150.00")); 
        dto.setNomeBeneficiario("Empresa Teste LTDA");

        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any())).thenReturn(transacao);
        when(pagamentoRepository.save(any())).thenReturn(pagamento);

        PagamentoBoletoModel resultado = service.realizarPagamento(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigoBarras()).isEqualTo(dto.getCodigoBarras());
        verify(pagamentoRepository).save(any());
    }

    // =========================================================
    //  pagarBoleto
    // =========================================================

    @Test
    @DisplayName("pagarBoleto: deve debitar saldo da conta do usuário logado")
    void pagarBoleto_deveDebitarSaldoDaConta() {
        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any())).thenReturn(transacao);
        when(pagamentoRepository.save(any())).thenReturn(pagamento);

        service.pagarBoleto("codigo-barras", new BigDecimal("300.00"), "Beneficiário");

        assertThat(conta.getSaldo()).isEqualByComparingTo(new BigDecimal("700.00"));
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("pagarBoleto: deve salvar transação e pagamento após débito")
    void pagarBoleto_deveSalvarTransacaoEPagamento() {
        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any())).thenReturn(transacao);
        when(pagamentoRepository.save(any())).thenReturn(pagamento);

        PagamentoBoletoModel resultado = service.pagarBoleto(
                "34191.23456", new BigDecimal("150.00"), "Empresa Teste LTDA");

        verify(transacaoRepository).save(any());
        verify(pagamentoRepository).save(any());
        assertThat(resultado.getNomeBeneficiario()).isEqualTo("Empresa Teste LTDA");
        assertThat(resultado.getCodigoBarras()).isEqualTo("34191.23456 78901.234567 89012.345678 1 12340000015000");
    }

    @Test
    @DisplayName("pagarBoleto: deve registrar transação com tipo PAGAMENTO_BOLETO")
    void pagarBoleto_deveRegistrarTransacaoComTipoCorreto() {
        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any())).thenAnswer(inv -> {
            TransacaoModel t = inv.getArgument(0);
            assertThat(t.getTipoTransacao().name()).isEqualTo("PAGAMENTO_BOLETO");
            return transacao;
        });
        when(pagamentoRepository.save(any())).thenReturn(pagamento);

        service.pagarBoleto("codigo", new BigDecimal("100.00"), "Beneficiário");

        verify(transacaoRepository).save(any());
    }

    @Test
    @DisplayName("pagarBoleto: deve lançar exceção quando saldo insuficiente")
    void pagarBoleto_deveLancarExcecaoQuandoSaldoInsuficiente() {
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
        when(contaRepository.findAll()).thenReturn(List.of()); // nenhuma conta

        assertThatThrownBy(() -> service.pagarBoleto("codigo", new BigDecimal("100.00"), "Beneficiário"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada");

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("pagarBoleto: deve lançar exceção quando e-mail do usuário logado não bate com nenhuma conta")
    void pagarBoleto_deveLancarExcecaoQuandoEmailNaoCorresponde() {
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
        when(contaRepository.findAll()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any())).thenReturn(transacao);
        when(pagamentoRepository.save(any())).thenReturn(pagamento);

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
}