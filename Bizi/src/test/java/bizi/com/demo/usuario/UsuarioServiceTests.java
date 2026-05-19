package bizi.com.demo.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.endereco.EnderecoRepository;
import bizi.com.demo.security.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private UsuarioService service;

    private UsuarioDto dto;
    private UsuarioModel usuario;
    private EnderecoModel endereco;

    @BeforeEach
    void setUp() {
        endereco = new EnderecoModel();
        endereco.setRua("Rua A");
        endereco.setNumero(123);
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01001000");

        dto = new UsuarioDto();
        dto.setNomeCompleto("João Silva");
        dto.setCpf("12345678900");
        dto.setEmail("joao@email.com");
        dto.setTelefone("11999999999");
        dto.setSenha("123456");
        dto.setRole(Role.ROLE_CLIENTE);
        dto.setEndereco(endereco);

        usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setNomeCompleto("João Silva");
        usuario.setCpf("12345678900");
        usuario.setEmail("joao@email.com");
        usuario.setTelefone("11999999999");
        usuario.setSenha("senha-criptografada");
        usuario.setRole(Role.ROLE_CLIENTE);
        usuario.setEndereco(endereco);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar usuário quando ID existe")
    void buscarPorId_deveRetornarUsuarioQuandoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioModel resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando ID não existe")
    void buscarPorId_deveLancarQuandoNaoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado com ID: 99");
    }

    @Test
    @DisplayName("criarUsuario: deve criar usuário com endereço e senha criptografada")
    void criarUsuario_deveCriarComEnderecoESenhaCriptografada() {
        when(usuarioRepository.existsByCpf("12345678900")).thenReturn(false);
        when(securityUtil.getRoleUsuarioLogado()).thenReturn(Role.ROLE_ADMIN);
        when(enderecoRepository.save(endereco)).thenReturn(endereco);
        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioModel resultado = service.criarUsuario(dto);

        assertThat(resultado.getNomeCompleto()).isEqualTo("João Silva");
        assertThat(resultado.getCpf()).isEqualTo("12345678900");
        assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
        assertThat(resultado.getTelefone()).isEqualTo("11999999999");
        assertThat(resultado.getSenha()).isEqualTo("senha-criptografada");
        assertThat(resultado.getRole()).isEqualTo(Role.ROLE_CLIENTE);
        assertThat(resultado.getEndereco()).isEqualTo(endereco);
        assertThat(resultado.getDataCadastro()).isNotNull();

        verify(enderecoRepository).save(endereco);
        verify(usuarioRepository).save(any(UsuarioModel.class));
    }

    @Test
    @DisplayName("criarUsuario: deve lançar exceção quando CPF já existe")
    void criarUsuario_deveLancarQuandoCpfJaExiste() {
        when(usuarioRepository.existsByCpf("12345678900")).thenReturn(true);

        assertThatThrownBy(() -> service.criarUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("criarUsuario: deve vincular responsável quando responsavelId foi informado")
    void criarUsuario_deveVincularResponsavelQuandoInformado() {
        UsuarioModel responsavel = new UsuarioModel();
        responsavel.setId(10L);
        responsavel.setCpf("99999999999");

        dto.setResponsavelId(10L);
        dto.setRole(Role.ROLE_FILHO);

        when(usuarioRepository.existsByCpf("12345678900")).thenReturn(false);
        when(securityUtil.getRoleUsuarioLogado()).thenReturn(Role.ROLE_ADMIN);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(responsavel));
        when(enderecoRepository.save(endereco)).thenReturn(endereco);
        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioModel resultado = service.criarUsuario(dto);

        assertThat(resultado.getRole()).isEqualTo(Role.ROLE_FILHO);
        assertThat(resultado.getResponsavel()).isEqualTo(responsavel);
    }

    @Test
    @DisplayName("criarUsuario: deve usar ROLE_CLIENTE quando role nula em autocadastro")
    void criarUsuario_deveUsarRoleClienteQuandoRoleNulaEmAutocadastro() {
        dto.setRole(null);

        when(usuarioRepository.existsByCpf("12345678900")).thenReturn(false);
        doThrow(new RuntimeException("Sem autenticação"))
                .when(securityUtil).getRoleUsuarioLogado();
        when(enderecoRepository.save(endereco)).thenReturn(endereco);
        when(passwordEncoder.encode("123456")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioModel resultado = service.criarUsuario(dto);

        assertThat(resultado.getRole()).isEqualTo(Role.ROLE_CLIENTE);
    }

    @Test
    @DisplayName("buscarPorCpf: deve retornar usuário quando CPF existe")
    void buscarPorCpf_deveRetornarUsuarioQuandoExiste() {
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        UsuarioModel resultado = service.buscarPorCpf("12345678900");

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    @DisplayName("buscarPorCpf: deve lançar exceção quando CPF não existe")
    void buscarPorCpf_deveLancarQuandoNaoExiste() {
        when(usuarioRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorCpf("00000000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado com CPF: 00000000000");
    }

    @Test
    @DisplayName("listarTodos: deve retornar todos os usuários")
    void listarTodos_deveRetornarTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario, new UsuarioModel()));

        List<UsuarioModel> resultado = service.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("atualizarUsuario: deve atualizar campos informados")
    void atualizarUsuario_deveAtualizarCamposInformados() {
        UsuarioDto updateDto = new UsuarioDto();
        updateDto.setNomeCompleto("João Atualizado");
        updateDto.setCpf("12345678900");
        updateDto.setEmail("novo@email.com");
        updateDto.setTelefone("11888888888");
        updateDto.setSenha("novaSenha");
        updateDto.setRole(Role.ROLE_ADMIN);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCriptografada");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioModel resultado = service.atualizarUsuario(1L, updateDto);

        assertThat(resultado.getNomeCompleto()).isEqualTo("João Atualizado");
        assertThat(resultado.getEmail()).isEqualTo("novo@email.com");
        assertThat(resultado.getTelefone()).isEqualTo("11888888888");
        assertThat(resultado.getSenha()).isEqualTo("novaSenhaCriptografada");
        assertThat(resultado.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("atualizarUsuario: deve lançar exceção quando novo CPF pertence a outro usuário")
    void atualizarUsuario_deveLancarQuandoCpfJaPertenceAOutroUsuario() {
        UsuarioDto updateDto = new UsuarioDto();
        updateDto.setCpf("99999999999");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByCpf("99999999999")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizarUsuario(1L, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF já cadastrado em outro usuário");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizarUsuario: deve salvar novo endereço quando informado")
    void atualizarUsuario_deveSalvarNovoEnderecoQuandoInformado() {
        EnderecoModel novoEndereco = new EnderecoModel();
        novoEndereco.setRua("Rua Nova");

        UsuarioDto updateDto = new UsuarioDto();
        updateDto.setEndereco(novoEndereco);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(enderecoRepository.save(novoEndereco)).thenReturn(novoEndereco);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioModel resultado = service.atualizarUsuario(1L, updateDto);

        assertThat(resultado.getEndereco()).isEqualTo(novoEndereco);
        verify(enderecoRepository).save(novoEndereco);
    }



    @Test
    @DisplayName("atualizarContato: deve atualizar email e telefone")
    void atualizarContato_deveAtualizarEmailETelefone() {
        ContatoDto contatoDto = new ContatoDto();
        contatoDto.setEmail("novo@email.com");
        contatoDto.setTelefone("11777777777");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        service.atualizarContato(1L, contatoDto);

        assertThat(usuario.getEmail()).isEqualTo("novo@email.com");
        assertThat(usuario.getTelefone()).isEqualTo("11777777777");

        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deletarUsuario: deve deletar quando ID existe")
    void deletarUsuario_deveDeletarQuandoExiste() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        service.deletarUsuario(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletarUsuario: deve lançar exceção quando ID não existe")
    void deletarUsuario_deveLancarQuandoNaoExiste() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletarUsuario(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado com ID: 99");

        verify(usuarioRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("deletarUsuarioPorCpf: deve buscar por CPF e deletar pelo ID")
    void deletarUsuarioPorCpf_deveBuscarPorCpfEDeletarPeloId() {
        usuario.setId(1L);

        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

        service.deletarUsuarioPorCpf("12345678900");

        verify(usuarioRepository).deleteById(1L);
    }
}