package bizi.com.demo.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.endereco.EnderecoRepository;
import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

class DatabaseSeederTest {

    @Test
    @DisplayName("run: deve criar admin quando CPF ainda não existe")
    void run_deveCriarAdminQuandoCpfNaoExiste() {
        UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
        EnderecoRepository enderecoRepository = Mockito.mock(EnderecoRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        DatabaseSeeder seeder = new DatabaseSeeder(usuarioRepository, enderecoRepository, passwordEncoder);

        when(usuarioRepository.existsByCpf("00000000000")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("senha-criptografada");
        when(enderecoRepository.save(any(EnderecoModel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> seeder.run())
                .doesNotThrowAnyException();

        verify(enderecoRepository).save(any(EnderecoModel.class));
        verify(usuarioRepository).save(any(UsuarioModel.class));
    }

    @Test
    @DisplayName("run: não deve criar admin quando CPF já existe")
    void run_naoDeveCriarAdminQuandoCpfJaExiste() {
        UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
        EnderecoRepository enderecoRepository = Mockito.mock(EnderecoRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        DatabaseSeeder seeder = new DatabaseSeeder(usuarioRepository, enderecoRepository, passwordEncoder);

        when(usuarioRepository.existsByCpf("00000000000")).thenReturn(true);

        assertThatCode(() -> seeder.run())
                .doesNotThrowAnyException();

        verify(enderecoRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("run: deve engolir exceção e não propagar erro")
    void run_deveEngolirExcecaoENaoPropagarErro() {
        UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
        EnderecoRepository enderecoRepository = Mockito.mock(EnderecoRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        DatabaseSeeder seeder = new DatabaseSeeder(usuarioRepository, enderecoRepository, passwordEncoder);

        when(usuarioRepository.existsByCpf("00000000000")).thenThrow(new RuntimeException("Banco fora"));

        assertThatCode(() -> seeder.run())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("run: deve preencher dados corretos do admin")
    void run_devePreencherDadosCorretosDoAdmin() {
        UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
        EnderecoRepository enderecoRepository = Mockito.mock(EnderecoRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        DatabaseSeeder seeder = new DatabaseSeeder(usuarioRepository, enderecoRepository, passwordEncoder);

        when(usuarioRepository.existsByCpf("00000000000")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("senha-criptografada");
        when(enderecoRepository.save(any(EnderecoModel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(inv -> {
            UsuarioModel admin = inv.getArgument(0);

            org.assertj.core.api.Assertions.assertThat(admin.getNomeCompleto()).isEqualTo("Administrador Bizi");
            org.assertj.core.api.Assertions.assertThat(admin.getCpf()).isEqualTo("00000000000");
            org.assertj.core.api.Assertions.assertThat(admin.getEmail()).isEqualTo("admin@bizi.com");
            org.assertj.core.api.Assertions.assertThat(admin.getTelefone()).isEqualTo("11999999999");
            org.assertj.core.api.Assertions.assertThat(admin.getSenha()).isEqualTo("senha-criptografada");
            org.assertj.core.api.Assertions.assertThat(admin.getRole()).isEqualTo(Role.ROLE_ADMIN);
            org.assertj.core.api.Assertions.assertThat(admin.getDataCadastro()).isNotNull();
            org.assertj.core.api.Assertions.assertThat(admin.getEndereco()).isNotNull();

            return admin;
        });

        seeder.run();

        verify(usuarioRepository).save(any(UsuarioModel.class));
    }
}