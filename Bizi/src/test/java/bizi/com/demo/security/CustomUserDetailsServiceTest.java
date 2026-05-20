package bizi.com.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("loadUserByUsername: deve retornar usuário quando CPF existe")
    void loadUserByUsername_deveRetornarUsuarioQuandoCpfExiste() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setCpf("12345678900");

        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        var resultado = service.loadUserByUsername("12345678900");

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    @DisplayName("loadUserByUsername: deve lançar UsernameNotFoundException quando CPF não existe")
    void loadUserByUsername_deveLancarQuandoCpfNaoExiste() {
        when(usuarioRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("00000000000"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado com CPF: 00000000000");
    }
}