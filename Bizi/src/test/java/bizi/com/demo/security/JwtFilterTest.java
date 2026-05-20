package bizi.com.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import bizi.com.demo.login.TokenService;
import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private JwtFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal: deve seguir cadeia sem autenticar quando token ausente")
    void doFilterInternal_deveSeguirSemAutenticarQuandoTokenAusente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService, never()).getClaimCpf(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("doFilterInternal: deve seguir cadeia sem autenticar quando header não é Bearer")
    void doFilterInternal_deveSeguirSemAutenticarQuandoHeaderNaoBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");
        request.addHeader("Authorization", "Basic abc123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService, never()).getClaimCpf(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("doFilterInternal: deve autenticar usuário quando token é válido e CPF existe")
    void doFilterInternal_deveAutenticarQuandoTokenValidoECpfExiste() throws Exception {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setCpf("12345678900");
        usuario.setRole(Role.ROLE_ADMIN);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/contas");
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getClaimCpf("token-valido")).thenReturn("12345678900");
        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(usuario);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("doFilterInternal: não deve autenticar quando CPF do token não existe")
    void doFilterInternal_naoDeveAutenticarQuandoCpfNaoExiste() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/contas");
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getClaimCpf("token-valido")).thenReturn("12345678900");
        when(repository.findByCpf("12345678900")).thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal: deve continuar cadeia quando tokenService lança exceção")
    void doFilterInternal_deveContinuarQuandoTokenServiceLancaExcecao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/contas");
        request.addHeader("Authorization", "Bearer token-invalido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getClaimCpf("token-invalido")).thenThrow(new RuntimeException("Token inválido"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}