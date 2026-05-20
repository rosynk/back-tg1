package bizi.com.demo.saque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.contaBancaria.ContaInexistenteException;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;

@ExtendWith(MockitoExtension.class)
class SaqueServiceTest {

    @Mock
    private ContaBancariaRepository contaRepository;

    @Mock
    private ContaBancariaService contaService;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private SaqueService service;

    private ContaBancariaModel conta;

    @BeforeEach
    void setUp() {
        conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setSaldo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("realizarSaque: deve debitar saldo e salvar transação")
    void realizarSaque_deveDebitarSaldoESalvarTransacao() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.realizarSaque(new BigDecimal("100.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("900.00");
        assertThat(resultado.getContaBancaria()).isEqualTo(conta);
        assertThat(resultado.getTipoTransacao()).isEqualTo(TipoTransacao.SAQUE);
        assertThat(resultado.getValor()).isEqualByComparingTo("100.00");
        assertThat(resultado.getDataHora()).isNotNull();

        verify(contaRepository).save(conta);
        verify(transacaoRepository).save(any(TransacaoModel.class));
    }

    @Test
    @DisplayName("realizarSaque: deve lançar exceção quando valor é zero")
    void realizarSaque_deveLancarQuandoValorZero() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));

        assertThatThrownBy(() -> service.realizarSaque(BigDecimal.ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("O valor do saque deve ser positivo");

        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarSaque: deve lançar exceção quando valor é negativo")
    void realizarSaque_deveLancarQuandoValorNegativo() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));

        assertThatThrownBy(() -> service.realizarSaque(new BigDecimal("-10.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("O valor do saque deve ser positivo");

        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarSaque: deve lançar exceção quando saldo insuficiente")
    void realizarSaque_deveLancarQuandoSaldoInsuficiente() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));

        assertThatThrownBy(() -> service.realizarSaque(new BigDecimal("1500.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarSaque: deve permitir saque com valor igual ao saldo")
    void realizarSaque_devePermitirValorIgualAoSaldo() {
        when(contaService.buscarMinhasContas()).thenReturn(List.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.realizarSaque(new BigDecimal("1000.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getTipoTransacao()).isEqualTo(TipoTransacao.SAQUE);

        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("realizarSaqueAdministrativo: deve debitar saldo e salvar transação")
    void realizarSaqueAdministrativo_deveDebitarSaldoESalvarTransacao() {
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.realizarSaqueAdministrativo(10L, new BigDecimal("250.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("750.00");
        assertThat(resultado.getContaBancaria()).isEqualTo(conta);
        assertThat(resultado.getTipoTransacao()).isEqualTo(TipoTransacao.SAQUE);
        assertThat(resultado.getValor()).isEqualByComparingTo("250.00");
        assertThat(resultado.getDataHora()).isNotNull();

        verify(contaRepository).save(conta);
        verify(transacaoRepository).save(any(TransacaoModel.class));
    }

    @Test
    @DisplayName("realizarSaqueAdministrativo: deve lançar exceção quando conta não existe")
    void realizarSaqueAdministrativo_deveLancarQuandoContaNaoExiste() {
        when(contaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizarSaqueAdministrativo(99L, new BigDecimal("100.00")))
                .isInstanceOf(ContaInexistenteException.class)
                .hasMessageContaining("A conta 99 não existe");

        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarSaqueAdministrativo: deve lançar exceção quando saldo insuficiente")
    void realizarSaqueAdministrativo_deveLancarQuandoSaldoInsuficiente() {
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.realizarSaqueAdministrativo(10L, new BigDecimal("1500.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarSaqueAdministrativo: deve permitir saque com valor igual ao saldo")
    void realizarSaqueAdministrativo_devePermitirValorIgualAoSaldo() {
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(TransacaoModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoModel resultado = service.realizarSaqueAdministrativo(10L, new BigDecimal("1000.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getTipoTransacao()).isEqualTo(TipoTransacao.SAQUE);

        verify(contaRepository).save(conta);
    }
}