package bizi.com.demo.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import bizi.com.demo.comunicacao.ComunicacaoService;
import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ComunicacaoService comunicacaoService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private UsuarioModel usuarioCliente;
    private UsuarioModel usuarioAdmin;

    @BeforeEach
    void setUp() {
        usuarioCliente = new UsuarioModel();
        usuarioCliente.setId(1L);
        usuarioCliente.setCpf("12345678900");
        usuarioCliente.setSenha("senhaHashCliente");
        usuarioCliente.setEmail("cliente@email.com");
        usuarioCliente.setRole(Role.ROLE_CLIENTE);

        usuarioAdmin = new UsuarioModel();
        usuarioAdmin.setId(2L);
        usuarioAdmin.setCpf("99988877766");
        usuarioAdmin.setSenha("senhaHashAdmin");
        usuarioAdmin.setEmail("admin@email.com");
        usuarioAdmin.setRole(Role.ROLE_ADMIN);
    }

    // =========================================================
    //  autenticar — CLIENTE
    // =========================================================

    @Test
    @DisplayName("autenticar: deve autenticar cliente via AuthenticationManager e retornar token")
    void autenticar_deveAutenticarClienteViaAuthManager() {
        LoginDto dto = new LoginDto("12345678900", "senha123");

        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioCliente));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(tokenService.gerarToken(usuarioCliente)).thenReturn("jwt-token-cliente");

        String resultado = authService.autenticar(dto);

        assertThat(resultado).isEqualTo("jwt-token-cliente");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("autenticar: deve lançar exceção quando CPF não encontrado")
    void autenticar_deveLancarExcecaoQuandoCpfNaoEncontrado() {
        LoginDto dto = new LoginDto("00000000000", "senha123");

        when(usuarioRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.autenticar(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF não encontrado");

        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("autenticar: deve lançar exceção quando AuthenticationManager rejeita cliente")
    void autenticar_deveLancarExcecaoQuandoSenhaClienteInvalida() {
        LoginDto dto = new LoginDto("12345678900", "senhaErrada");

        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioCliente));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("CPF ou senha incorretos."));

        assertThatThrownBy(() -> authService.autenticar(dto))
                .isInstanceOf(BadCredentialsException.class);

        verify(tokenService, never()).gerarToken(any());
    }

    // =========================================================
    //  autenticar — ADMIN
    // =========================================================

    @Test
    @DisplayName("autenticar: deve autenticar admin via PasswordEncoder sem passar pelo AuthenticationManager")
    void autenticar_deveAutenticarAdminViaPasswordEncoder() {
        LoginDto dto = new LoginDto("99988877766", "senhaAdmin");

        when(usuarioRepository.findByCpf("99988877766")).thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.matches("senhaAdmin", "senhaHashAdmin")).thenReturn(true);
        when(tokenService.gerarToken(usuarioAdmin)).thenReturn("jwt-token-admin");

        String resultado = authService.autenticar(dto);

        assertThat(resultado).isEqualTo("jwt-token-admin");
        verify(passwordEncoder).matches("senhaAdmin", "senhaHashAdmin");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("autenticar: deve lançar BadCredentialsException quando senha do admin está errada")
    void autenticar_deveLancarExcecaoQuandoSenhaAdminInvalida() {
        LoginDto dto = new LoginDto("99988877766", "senhaErrada");

        when(usuarioRepository.findByCpf("99988877766")).thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.matches("senhaErrada", "senhaHashAdmin")).thenReturn(false);

        assertThatThrownBy(() -> authService.autenticar(dto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("CPF ou senha incorretos");

        verify(tokenService, never()).gerarToken(any());
    }

    // =========================================================
    //  solicitarCodigoRecuperacao
    // =========================================================

    @Test
    @DisplayName("solicitarCodigoRecuperacao: deve gerar código, salvar e enviar e-mail")
    void solicitarCodigo_deveGerarCodigoSalvarEEnviarEmail() {
        when(usuarioRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(usuarioCliente));

        authService.solicitarCodigoRecuperacao("cliente@email.com");

        assertThat(usuarioCliente.getCodigoRecuperacao()).isNotNull();
        assertThat(usuarioCliente.getCodigoRecuperacao()).hasSize(6);
        assertThat(usuarioCliente.getDataExpiracaoCodigo()).isAfter(LocalDateTime.now());
        verify(usuarioRepository).save(usuarioCliente);
        verify(comunicacaoService).enviarEmailRecuperacao(
                anyString(), anyString()
        );
    }

    @Test
    @DisplayName("solicitarCodigoRecuperacao: deve definir expiração de 15 minutos no futuro")
    void solicitarCodigo_deveDefinirExpiracaoDe15Minutos() {
        when(usuarioRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(usuarioCliente));

        authService.solicitarCodigoRecuperacao("cliente@email.com");

        LocalDateTime agora = LocalDateTime.now();
        assertThat(usuarioCliente.getDataExpiracaoCodigo())
                .isAfter(agora.plusMinutes(14))
                .isBefore(agora.plusMinutes(16));
    }

    @Test
    @DisplayName("solicitarCodigoRecuperacao: deve lançar exceção quando e-mail não encontrado")
    void solicitarCodigo_deveLancarExcecaoQuandoEmailNaoEncontrado() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.solicitarCodigoRecuperacao("naoexiste@email.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("E-mail não encontrado");

        verify(usuarioRepository, never()).save(any());
        verify(comunicacaoService, never()).enviarEmailRecuperacao(anyString(), anyString());
    }

    // =========================================================
    //  redefinirSenha
    // =========================================================

    @Test
    @DisplayName("redefinirSenha: deve atualizar senha e limpar código quando dados válidos")
    void redefinirSenha_deveAtualizarSenhaELimparCodigo() {
        usuarioCliente.setCodigoRecuperacao("123456");
        usuarioCliente.setDataExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(usuarioCliente));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaHash");

        authService.redefinirSenha("cliente@email.com", "123456", "novaSenha123");

        assertThat(usuarioCliente.getSenha()).isEqualTo("novaSenhaHash");
        assertThat(usuarioCliente.getCodigoRecuperacao()).isNull();
        assertThat(usuarioCliente.getDataExpiracaoCodigo()).isNull();
        verify(usuarioRepository).save(usuarioCliente);
    }

    @Test
    @DisplayName("redefinirSenha: deve lançar exceção quando usuário não encontrado")
    void redefinirSenha_deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.redefinirSenha("naoexiste@email.com", "123456", "novaSenha"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("redefinirSenha: deve lançar exceção quando código é nulo")
    void redefinirSenha_deveLancarExcecaoQuandoCodigoNulo() {
        usuarioCliente.setCodigoRecuperacao(null);
        usuarioCliente.setDataExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(usuarioCliente));

        assertThatThrownBy(() -> authService.redefinirSenha("cliente@email.com", "123456", "novaSenha"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Código de validação incorreto");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("redefinirSenha: deve lançar exceção quando código não bate")
    void redefinirSenha_deveLancarExcecaoQuandoCodigoIncorreto() {
        usuarioCliente.setCodigoRecuperacao("999999");
        usuarioCliente.setDataExpiracaoCodigo(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(usuarioCliente));

        assertThatThrownBy(() -> authService.redefinirSenha("cliente@email.com", "111111", "novaSenha"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Código de validação incorreto");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("redefinirSenha: deve lançar exceção quando código expirado")
    void redefinirSenha_deveLancarExcecaoQuandoCodigoExpirado() {
        usuarioCliente.setCodigoRecuperacao("123456");
        usuarioCliente.setDataExpiracaoCodigo(LocalDateTime.now().minusMinutes(1)); // já expirou

        when(usuarioRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(usuarioCliente));

        assertThatThrownBy(() -> authService.redefinirSenha("cliente@email.com", "123456", "novaSenha"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirou");

        verify(usuarioRepository, never()).save(any());
    }
}