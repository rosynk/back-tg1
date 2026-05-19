package bizi.com.demo.contabancaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.contaBancaria.ContaBancariaDto;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.contaBancaria.TipoConta;
import bizi.com.demo.security.SecurityUtil;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioNotFoundException;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ContaBancariaServiceTest {

    @Mock private ContaBancariaRepository contaBancariaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks
    private ContaBancariaService service;

    private UsuarioModel usuario;
    private ContaBancariaModel conta;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setCpf("12345678900");

        conta = new ContaBancariaModel();
        conta.setId(10L);
        conta.setUsuario(usuario);
        conta.setSaldo(new BigDecimal("1000.00"));
        conta.setNumeroAgencia("0001");
        conta.setNumeroConta("123456");
    }

    // =========================================================
    //  criarConta
    // =========================================================

    @Test
    @DisplayName("criarConta: deve criar conta vinculando usuário pelo ID do DTO")
    void criarConta_deveVincularUsuarioPeloIdDoDto() {
        ContaBancariaDto dto = new ContaBancariaDto();
        dto.setUsuarioId(1L);
        dto.setNumeroAgencia("0001");
        dto.setTipoConta(TipoConta.CORRENTE);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(contaBancariaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaBancariaModel resultado = service.criarConta(dto);

        assertThat(resultado.getUsuario()).isEqualTo(usuario);
        assertThat(resultado.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getStatusConta()).isTrue();
        assertThat(resultado.getNumeroAgencia()).isEqualTo("0001");
        verify(contaBancariaRepository).save(any());
    }

    @Test
    @DisplayName("criarConta: deve usar agência '0001' quando DTO não informa agência")
    void criarConta_deveUsarAgenciaDefaultQuandoNaoInformada() {
        ContaBancariaDto dto = new ContaBancariaDto();
        dto.setUsuarioId(1L);
        dto.setNumeroAgencia(null); // não informada
        dto.setTipoConta(TipoConta.POUPANCA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(contaBancariaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaBancariaModel resultado = service.criarConta(dto);

        assertThat(resultado.getNumeroAgencia()).isEqualTo("0001");
    }

    @Test
    @DisplayName("criarConta: deve buscar usuário logado quando DTO não tem usuarioId")
    void criarConta_deveBuscarUsuarioLogadoQuandoIdNulo() {
        ContaBancariaDto dto = new ContaBancariaDto();
        dto.setUsuarioId(null);
        dto.setTipoConta(TipoConta.CORRENTE);

        when(securityUtil.getUsuarioLogado()).thenReturn(usuario);
        when(contaBancariaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaBancariaModel resultado = service.criarConta(dto);

        assertThat(resultado.getUsuario()).isEqualTo(usuario);
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    @DisplayName("criarConta: deve lançar exceção quando usuário não encontrado pelo ID")
    void criarConta_deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        ContaBancariaDto dto = new ContaBancariaDto();
        dto.setUsuarioId(999L);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarConta(dto))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // =========================================================
    //  depositar
    // =========================================================

    @Test
    @DisplayName("depositar: deve somar valor ao saldo existente")
    void depositar_deveSomarValorAoSaldo() {
        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(contaBancariaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.depositar(10L, new BigDecimal("500.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("depositar: deve lançar exceção para valor zero")
    void depositar_deveLancarExcecaoParaValorZero() {
        assertThatThrownBy(() -> service.depositar(10L, BigDecimal.ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("depositar: deve lançar exceção para valor negativo")
    void depositar_deveLancarExcecaoParaValorNegativo() {
        assertThatThrownBy(() -> service.depositar(10L, new BigDecimal("-100.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("depositar: deve lançar exceção para valor nulo")
    void depositar_deveLancarExcecaoParaValorNulo() {
        assertThatThrownBy(() -> service.depositar(10L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("maior que zero");
    }

    // =========================================================
    //  transferir (PIX)
    // =========================================================

    @Test
    @DisplayName("transferir: deve debitar origem e creditar destino corretamente")
    void transferir_deveDebitarOrigemECreditarDestino() {
        ContaBancariaModel destino = new ContaBancariaModel();
        destino.setId(20L);
        destino.setUsuario(usuario);
        destino.setSaldo(new BigDecimal("200.00"));

        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(contaBancariaRepository.findById(20L)).thenReturn(Optional.of(destino));

        service.transferir(10L, 20L, new BigDecimal("300.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(destino.getSaldo()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(contaBancariaRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("transferir: deve lançar exceção quando saldo insuficiente")
    void transferir_deveLancarExcecaoSaldoInsuficiente() {
        ContaBancariaModel destino = new ContaBancariaModel();
        destino.setId(20L);
        destino.setUsuario(usuario);
        destino.setSaldo(BigDecimal.ZERO);

        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(contaBancariaRepository.findById(20L)).thenReturn(Optional.of(destino));

        assertThatThrownBy(() -> service.transferir(10L, 20L, new BigDecimal("9999.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(contaBancariaRepository, never()).save(any());
    }

    @Test
    @DisplayName("transferir: deve lançar exceção quando origem e destino são iguais")
    void transferir_deveLancarExcecaoContasIguais() {
        assertThatThrownBy(() -> service.transferir(10L, 10L, new BigDecimal("100.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contas iguais");
    }

    @Test
    @DisplayName("transferir: deve lançar exceção para valor zero")
    void transferir_deveLancarExcecaoParaValorZero() {
        assertThatThrownBy(() -> service.transferir(10L, 20L, BigDecimal.ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("maior que zero");
    }

    // =========================================================
    //  transferirViaTed — regras de horário e dia
    // =========================================================

    @Test
    @DisplayName("TED: deve transferir em horário válido (terça às 10h)")
    void ted_deveTransferirEmHorarioValido() {
        ContaBancariaModel destino = new ContaBancariaModel();
        destino.setId(20L);
        destino.setUsuario(usuario);
        destino.setSaldo(BigDecimal.ZERO);

        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(contaBancariaRepository.findById(20L)).thenReturn(Optional.of(destino));

        // Simula terça-feira às 10h — dentro do horário permitido
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            LocalDateTime terca10h = LocalDateTime.of(2025, 1, 7, 10, 0); // terça
            mock.when(LocalDateTime::now).thenReturn(terca10h);

            assertThatNoException().isThrownBy(
                () -> service.transferirViaTed(10L, 20L, new BigDecimal("100.00"))
            );
        }
    }

    @Test
    @DisplayName("TED: deve lançar exceção aos sábados")
    void ted_deveLancarExcecaoNoSabado() {
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            LocalDateTime sabado = LocalDateTime.of(2025, 1, 4, 10, 0); // sábado
            mock.when(LocalDateTime::now).thenReturn(sabado);

            assertThatThrownBy(() -> service.transferirViaTed(10L, 20L, new BigDecimal("100.00")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("finais de semana");
        }
    }

    @Test
    @DisplayName("TED: deve lançar exceção aos domingos")
    void ted_deveLancarExcecaoNoDomingo() {
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            LocalDateTime domingo = LocalDateTime.of(2025, 1, 5, 14, 0); // domingo
            mock.when(LocalDateTime::now).thenReturn(domingo);

            assertThatThrownBy(() -> service.transferirViaTed(10L, 20L, new BigDecimal("100.00")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("finais de semana");
        }
    }

    @Test
    @DisplayName("TED: deve lançar exceção antes das 9h")
    void ted_deveLancarExcecaoAntesDas9h() {
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            LocalDateTime terca8h = LocalDateTime.of(2025, 1, 7, 8, 59); // terça 08:59
            mock.when(LocalDateTime::now).thenReturn(terca8h);

            assertThatThrownBy(() -> service.transferirViaTed(10L, 20L, new BigDecimal("100.00")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Horário de TED encerrado");
        }
    }

    @Test
    @DisplayName("TED: deve lançar exceção às 17h ou depois")
    void ted_deveLancarExcecaoAs17hOuDepois() {
        try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            LocalDateTime terca17h = LocalDateTime.of(2025, 1, 7, 17, 0); // terça 17:00
            mock.when(LocalDateTime::now).thenReturn(terca17h);

            assertThatThrownBy(() -> service.transferirViaTed(10L, 20L, new BigDecimal("100.00")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Horário de TED encerrado");
        }
    }

    // =========================================================
    //  realizarAutoDepositoLogado
    // =========================================================

    @Test
    @DisplayName("autoDepositoLogado: deve depositar na conta do usuário logado")
    void autoDepositoLogado_deveDepositarNaContaDoUsuarioLogado() {
        when(securityUtil.getUsuarioLogado()).thenReturn(usuario);
        when(contaBancariaRepository.findByUsuarioId(1L)).thenReturn(List.of(conta));
        when(contaBancariaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.realizarAutoDepositoLogado(new BigDecimal("250.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo(new BigDecimal("1250.00"));
    }

    @Test
    @DisplayName("autoDepositoLogado: deve lançar exceção quando usuário não tem conta")
    void autoDepositoLogado_deveLancarExcecaoSemConta() {
        when(securityUtil.getUsuarioLogado()).thenReturn(usuario);
        when(contaBancariaRepository.findByUsuarioId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.realizarAutoDepositoLogado(new BigDecimal("100.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nenhuma conta encontrada");
    }

    @Test
    @DisplayName("autoDepositoLogado: deve lançar exceção para valor negativo")
    void autoDepositoLogado_deveLancarExcecaoParaValorNegativo() {
        assertThatThrownBy(() -> service.realizarAutoDepositoLogado(new BigDecimal("-50.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("maior que zero");
    }

    // =========================================================
    //  buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar conta quando existe")
    void buscarPorId_deveRetornarContaQuandoExiste() {
        when(contaBancariaRepository.findById(10L)).thenReturn(Optional.of(conta));

        ContaBancariaModel resultado = service.buscarPorId(10L);

        assertThat(resultado).isEqualTo(conta);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando conta não existe")
    void buscarPorId_deveLancarExcecaoQuandoNaoExiste() {
        when(contaBancariaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(ContaBancariaNotFoundException.class)
                .hasMessageContaining("Conta não encontrada");
    }

    // =========================================================
    //  deletarConta
    // =========================================================

    @Test
    @DisplayName("deletarConta: deve deletar quando conta existe")
    void deletarConta_deveDeletarQuandoExiste() {
        when(contaBancariaRepository.existsById(10L)).thenReturn(true);

        service.deletarConta(10L);

        verify(contaBancariaRepository).deleteById(10L);
    }

    @Test
    @DisplayName("deletarConta: deve lançar exceção quando conta não existe")
    void deletarConta_deveLancarExcecaoQuandoNaoExiste() {
        when(contaBancariaRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletarConta(999L))
                .isInstanceOf(ContaBancariaNotFoundException.class);

        verify(contaBancariaRepository, never()).deleteById(any());
    }

    // =========================================================
    //  buscarPorAgenciaENumeroConta
    // =========================================================

    @Test
    @DisplayName("buscarPorAgenciaENumeroConta: deve retornar conta quando encontrada")
    void buscarPorAgenciaEConta_deveRetornarContaQuandoEncontrada() {
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("0001", "123456"))
                .thenReturn(Optional.of(conta));

        Optional<ContaBancariaModel> resultado = service.buscarPorAgenciaENumeroConta("0001", "123456");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNumeroConta()).isEqualTo("123456");
    }

    @Test
    @DisplayName("buscarPorAgenciaENumeroConta: deve retornar vazio quando não encontrada")
    void buscarPorAgenciaEConta_deveRetornarVazioQuandoNaoEncontrada() {
        when(contaBancariaRepository.findByNumeroAgenciaAndNumeroConta("9999", "000000"))
                .thenReturn(Optional.empty());

        Optional<ContaBancariaModel> resultado = service.buscarPorAgenciaENumeroConta("9999", "000000");

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    //  listarTodas / buscarMinhasContas
    // =========================================================

    @Test
    @DisplayName("listarTodas: deve retornar todas as contas do repositório")
    void listarTodas_deveRetornarTodasAsContas() {
        when(contaBancariaRepository.findAll()).thenReturn(List.of(conta, new ContaBancariaModel()));

        List<ContaBancariaModel> resultado = service.listarTodas();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("buscarMinhasContas: deve retornar contas do usuário logado")
    void buscarMinhasContas_deveRetornarContasDoUsuarioLogado() {
        when(securityUtil.getUsuarioLogado()).thenReturn(usuario);
        when(contaBancariaRepository.findByUsuarioId(1L)).thenReturn(List.of(conta));

        List<ContaBancariaModel> resultado = service.buscarMinhasContas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(conta);
    }
}