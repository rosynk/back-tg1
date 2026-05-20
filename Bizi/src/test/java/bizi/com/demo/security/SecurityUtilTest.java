package bizi.com.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;

class SecurityUtilTest {

    private final SecurityUtil securityUtil = new SecurityUtil();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getUsuarioLogado: deve retornar usuário autenticado")
    void getUsuarioLogado_deveRetornarUsuarioAutenticado() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setCpf("12345678900");
        usuario.setRole(Role.ROLE_CLIENTE);

        autenticar(usuario);

        UsuarioModel resultado = securityUtil.getUsuarioLogado();

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    @DisplayName("getUsuarioLogado: deve lançar exceção quando não há autenticação")
    void getUsuarioLogado_deveLancarQuandoNaoHaAutenticacao() {
        assertThatThrownBy(() -> securityUtil.getUsuarioLogado())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Não há nenhum usuário autenticado");
    }

    @Test
    @DisplayName("getUsuarioLogado: deve lançar exceção quando principal não é UsuarioModel")
    void getUsuarioLogado_deveLancarQuandoPrincipalNaoEhUsuarioModel() {
        var authentication = new UsernamePasswordAuthenticationToken("usuario", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(() -> securityUtil.getUsuarioLogado())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao recuperar os dados do usuário logado");
    }

    @Test
    @DisplayName("getRoleUsuarioLogado: deve retornar role do usuário")
    void getRoleUsuarioLogado_deveRetornarRole() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRole(Role.ROLE_CLIENTE);

        autenticar(usuario);

        Role resultado = securityUtil.getRoleUsuarioLogado();

        assertThat(resultado).isEqualTo(Role.ROLE_CLIENTE);
    }

    @Test
    @DisplayName("isAdmin: deve retornar true quando usuário é admin")
    void isAdmin_deveRetornarTrueQuandoAdmin() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRole(Role.ROLE_ADMIN);

        autenticar(usuario);

        assertThat(securityUtil.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("isAdmin: deve retornar false quando usuário não é admin")
    void isAdmin_deveRetornarFalseQuandoNaoAdmin() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRole(Role.ROLE_CLIENTE);

        autenticar(usuario);

        assertThat(securityUtil.isAdmin()).isFalse();
    }

    private void autenticar(UsuarioModel usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}