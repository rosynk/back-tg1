package bizi.com.demo.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.endereco.EnderecoModel;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    @Test
    @DisplayName("criarUsuario: deve retornar 201 quando usuário criado")
    void criarUsuario_deveRetornar201QuandoCriado() {
        UsuarioDto dto = new UsuarioDto();
        UsuarioModel usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setCpf("12345678900");

        when(usuarioService.criarUsuario(dto)).thenReturn(usuario);

        ResponseEntity<?> response = controller.criarUsuario(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("criarUsuario: deve retornar 400 quando service lança RuntimeException")
    void criarUsuario_deveRetornar400QuandoErro() {
        UsuarioDto dto = new UsuarioDto();

        when(usuarioService.criarUsuario(dto)).thenThrow(new RuntimeException("CPF já cadastrado!"));

        ResponseEntity<?> response = controller.criarUsuario(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        UsuarioController.ErrorMessage body = (UsuarioController.ErrorMessage) response.getBody();
        assertThat(body.getMessage()).isEqualTo("CPF já cadastrado!");
    }

    @Test
    @DisplayName("buscarPorCpf: deve retornar 200 com usuário")
    void buscarPorCpf_deveRetornar200ComUsuario() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setCpf("12345678900");

        when(usuarioService.buscarPorCpf("12345678900")).thenReturn(usuario);

        ResponseEntity<UsuarioModel> response = controller.buscarPorCpf("12345678900");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar 200 com usuário")
    void buscarPorId_deveRetornar200ComUsuario() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setId(1L);

        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        ResponseEntity<UsuarioModel> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("listarTodos: deve retornar 200 com lista de usuários")
    void listarTodos_deveRetornar200ComLista() {
        List<UsuarioModel> usuarios = List.of(new UsuarioModel(), new UsuarioModel());

        when(usuarioService.listarTodos()).thenReturn(usuarios);

        ResponseEntity<List<UsuarioModel>> response = controller.listarTodos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("atualizarUsuario: deve retornar 200 com usuário atualizado")
    void atualizarUsuario_deveRetornar200ComUsuarioAtualizado() {
        UsuarioDto dto = new UsuarioDto();
        UsuarioModel usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setNomeCompleto("Nome Atualizado");

        when(usuarioService.atualizarUsuario(1L, dto)).thenReturn(usuario);

        ResponseEntity<?> response = controller.atualizarUsuario(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("atualizarUsuario: deve retornar 400 quando service lança RuntimeException")
    void atualizarUsuario_deveRetornar400QuandoErro() {
        UsuarioDto dto = new UsuarioDto();

        when(usuarioService.atualizarUsuario(1L, dto))
                .thenThrow(new RuntimeException("CPF já cadastrado em outro usuário"));

        ResponseEntity<?> response = controller.atualizarUsuario(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        UsuarioController.ErrorMessage body = (UsuarioController.ErrorMessage) response.getBody();
        assertThat(body.getMessage()).isEqualTo("CPF já cadastrado em outro usuário");
    }

    @Test
    @DisplayName("deletarUsuario: deve retornar 204 quando deletado")
    void deletarUsuario_deveRetornar204() {
        doNothing().when(usuarioService).deletarUsuario(1L);

        ResponseEntity<Void> response = controller.deletarUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("deletarUsuarioPorCpf: deve retornar 204 quando deletado")
    void deletarUsuarioPorCpf_deveRetornar204() {
        doNothing().when(usuarioService).deletarUsuarioPorCpf("12345678900");

        ResponseEntity<Void> response = controller.deletarUsuarioPorCpf("12345678900");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("atualizarEndereco: deve retornar 200 quando endereço atualizado")
    void atualizarEndereco_deveRetornar200QuandoSucesso() {
        UsuarioModel usuarioLogado = new UsuarioModel();
        usuarioLogado.setId(1L);

        EnderecoModel endereco = new EnderecoModel();

        doNothing().when(usuarioService).atualizarEndereco(1L, endereco);

        ResponseEntity<?> response = controller.atualizarEndereco(usuarioLogado, endereco);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UsuarioController.ErrorMessage body = (UsuarioController.ErrorMessage) response.getBody();
        assertThat(body.getMessage()).isEqualTo("Endereço atualizado com sucesso.");
    }

    @Test
    @DisplayName("atualizarEndereco: deve retornar 400 quando service lança exceção")
    void atualizarEndereco_deveRetornar400QuandoErro() {
        UsuarioModel usuarioLogado = new UsuarioModel();
        usuarioLogado.setId(1L);

        EnderecoModel endereco = new EnderecoModel();

        doThrow(new RuntimeException("CEP inválido"))
                .when(usuarioService).atualizarEndereco(1L, endereco);

        ResponseEntity<?> response = controller.atualizarEndereco(usuarioLogado, endereco);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        UsuarioController.ErrorMessage body = (UsuarioController.ErrorMessage) response.getBody();
        assertThat(body.getMessage()).isEqualTo("CEP inválido");
    }

    @Test
    @DisplayName("atualizarContato: deve retornar 200 quando contato atualizado")
    void atualizarContato_deveRetornar200QuandoSucesso() {
        UsuarioModel usuarioLogado = new UsuarioModel();
        usuarioLogado.setId(1L);

        ContatoDto contatoDto = new ContatoDto();

        doNothing().when(usuarioService).atualizarContato(1L, contatoDto);

        ResponseEntity<?> response = controller.atualizarContato(usuarioLogado, contatoDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UsuarioController.ErrorMessage body = (UsuarioController.ErrorMessage) response.getBody();
        assertThat(body.getMessage()).isEqualTo("Contato atualizado com sucesso.");
    }

    @Test
    @DisplayName("atualizarContato: deve retornar 400 quando service lança exceção")
    void atualizarContato_deveRetornar400QuandoErro() {
        UsuarioModel usuarioLogado = new UsuarioModel();
        usuarioLogado.setId(1L);

        ContatoDto contatoDto = new ContatoDto();

        doThrow(new RuntimeException("Telefone inválido"))
                .when(usuarioService).atualizarContato(1L, contatoDto);

        ResponseEntity<?> response = controller.atualizarContato(usuarioLogado, contatoDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        UsuarioController.ErrorMessage body = (UsuarioController.ErrorMessage) response.getBody();
        assertThat(body.getMessage()).isEqualTo("Telefone inválido");
    }
}