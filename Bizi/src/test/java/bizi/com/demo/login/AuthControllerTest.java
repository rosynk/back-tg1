package bizi.com.demo.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthController controller;

    private UsuarioModel usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setCpf("12345678900");
        usuario.setRole(Role.ROLE_CLIENTE);
    }

    // =========================================================
    //  POST /api/auth/login — login
    // =========================================================

    @Test
    @DisplayName("login: deve retornar 200 com token e role quando credenciais válidas")
    void login_deveRetornar200ComTokenERole() {
        LoginDto dto = new LoginDto("12345678900", "senha123");

        when(authService.autenticar(dto)).thenReturn("jwt-token");
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        ResponseEntity<TokenResponseDto> response = controller.login(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().token()).isEqualTo("jwt-token");
        assertThat(response.getBody().role()).isEqualTo("ROLE_CLIENTE");
    }

    @Test
    @DisplayName("login: deve retornar role ROLE_CLIENTE como fallback quando CPF não encontrado no repositório")
    void login_deveRetornarRoleDefaultQuandoCpfNaoEncontradoNoRepositorio() {
        LoginDto dto = new LoginDto("12345678900", "senha123");

        when(authService.autenticar(dto)).thenReturn("jwt-token");
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        ResponseEntity<TokenResponseDto> response = controller.login(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().role()).isEqualTo("ROLE_CLIENTE");
    }

    @Test
    @DisplayName("login: deve retornar role ROLE_ADMIN quando usuário é admin")
    void login_deveRetornarRoleAdminQuandoUsuarioAdmin() {
        usuario.setRole(Role.ROLE_ADMIN);
        LoginDto dto = new LoginDto("12345678900", "senhaAdmin");

        when(authService.autenticar(dto)).thenReturn("jwt-admin-token");
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        ResponseEntity<TokenResponseDto> response = controller.login(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().role()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getBody().token()).isEqualTo("jwt-admin-token");
    }

    // =========================================================
    //  POST /api/auth/recuperar-senha — solicitarCodigo
    // =========================================================

    @Test
    @DisplayName("solicitarCodigo: deve retornar 200 com mensagem de confirmação")
    void solicitarCodigo_deveRetornar200ComMensagemDeConfirmacao() {
        RecuperacaoRequestDto dto = new RecuperacaoRequestDto("cliente@email.com");
        doNothing().when(authService).solicitarCodigoRecuperacao("cliente@email.com");

        ResponseEntity<String> response = controller.solicitarCodigo(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Código enviado");
    }

    @Test
    @DisplayName("solicitarCodigo: deve propagar exceção quando e-mail não encontrado")
    void solicitarCodigo_devePropagar404QuandoEmailNaoEncontrado() {
        RecuperacaoRequestDto dto = new RecuperacaoRequestDto("naoexiste@email.com");
        doThrow(new RuntimeException("E-mail não encontrado na base de dados."))
                .when(authService).solicitarCodigoRecuperacao("naoexiste@email.com");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.solicitarCodigo(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("E-mail não encontrado");
    }

    // =========================================================
    //  POST /api/auth/redefinir-senha — redefinirSenha
    // =========================================================

    @Test
    @DisplayName("redefinirSenha: deve retornar 200 com mensagem de sucesso quando dados válidos")
    void redefinirSenha_deveRetornar200QuandoSucesso() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("cliente@email.com", "123456", "novaSenha123");
        doNothing().when(authService).redefinirSenha("cliente@email.com", "123456", "novaSenha123");

        ResponseEntity<String> response = controller.redefinirSenha(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Senha atualizada com sucesso");
    }

    @Test
    @DisplayName("redefinirSenha: deve propagar exceção quando código inválido")
    void redefinirSenha_devePropagar400QuandoCodigoInvalido() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("cliente@email.com", "000000", "novaSenha123");
        doThrow(new RuntimeException("Código de validação incorreto."))
                .when(authService).redefinirSenha("cliente@email.com", "000000", "novaSenha123");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.redefinirSenha(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Código de validação incorreto");
    }

    @Test
    @DisplayName("redefinirSenha: deve propagar exceção quando código expirado")
    void redefinirSenha_devePropagar400QuandoCodigoExpirado() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("cliente@email.com", "123456", "novaSenha");
        doThrow(new RuntimeException("Este código de recuperação já expirou."))
                .when(authService).redefinirSenha("cliente@email.com", "123456", "novaSenha");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.redefinirSenha(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirou");
    }

    // =========================================================
    //  ExceptionHandlers
    // =========================================================

    @Test
    @DisplayName("handleDisabled: deve retornar 403 com mensagem da exceção")
    void handleDisabled_deveRetornar403() {
        DisabledException ex = new DisabledException("Usuário inativo. Aguarde aprovação.");

        ResponseEntity<String> response = controller.handleDisabled(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Usuário inativo");
    }

    @Test
    @DisplayName("handleBadCredentials: deve retornar 403 com mensagem padrão")
    void handleBadCredentials_deveRetornar403ComMensagemPadrao() {
        BadCredentialsException ex = new BadCredentialsException("qualquer mensagem interna");

        ResponseEntity<String> response = controller.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("CPF ou senha incorretos.");
    }
}