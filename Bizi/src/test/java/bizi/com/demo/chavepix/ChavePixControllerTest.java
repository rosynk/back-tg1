package bizi.com.demo.chavepix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.chavePix.ChavePixController;
import bizi.com.demo.chavePix.ChavePixDto;
import bizi.com.demo.chavePix.ChavePixModel;
import bizi.com.demo.chavePix.ChavePixService;
import bizi.com.demo.chavePix.TipoChave;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.validacoes.external.ChavePixApiResponse;

@ExtendWith(MockitoExtension.class)
class ChavePixControllerTest {

    @Mock
    private ChavePixService service;

    @InjectMocks
    private ChavePixController controller;

    private UsuarioModel usuarioLogado;

    @BeforeEach
    void setUp() {
        usuarioLogado = new UsuarioModel();
        usuarioLogado.setCpf("12345678900");
    }

    // =========================================================
    //  POST /api/chaves-pix — cadastrar
    // =========================================================

    @Test
    @DisplayName("cadastrar: deve retornar 200 quando chave criada com isSucesso")
    void cadastrar_deveRetornar200QuandoSucesso() {
        ChavePixModel novaChave = new ChavePixModel();
        when(service.buscarIdContaPorUsuario("12345678900")).thenReturn(1L);
        when(service.cadastrarChave(eq(1L), eq(TipoChave.CPF), eq(usuarioLogado))).thenReturn(novaChave);

        ChavePixDto dto = new ChavePixDto(TipoChave.CPF);
        ResponseEntity<ChavePixApiResponse> response = controller.cadastrar(usuarioLogado, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSucesso()).isTrue();
        assertThat(response.getBody().getMensagem()).isEqualTo("Chave cadastrada!");
    }

    @Test
    @DisplayName("cadastrar: deve retornar 400 quando serviço lança exceção")
    void cadastrar_deveRetornar400QuandoErro() {
        when(service.buscarIdContaPorUsuario(anyString())).thenThrow(new RuntimeException("Limite de 5 chaves atingido."));

        ChavePixDto dto = new ChavePixDto(TipoChave.CPF);
        ResponseEntity<ChavePixApiResponse> response = controller.cadastrar(usuarioLogado, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).isEqualTo("Limite de 5 chaves atingido.");
    }

    // =========================================================
    //  GET /api/chaves-pix/buscar?chave= — buscarPorChave
    // =========================================================

    @Test
    @DisplayName("buscarPorChave: deve retornar 200 com dados do titular quando chave existe")
    void buscarPorChave_deveRetornar200QuandoChaveExiste() {
        // Monta um grafo mínimo de objetos para o controller conseguir ler os campos
        UsuarioModel titular = new UsuarioModel();
        titular.setNomeCompleto("João Silva");

        var contaMock = new bizi.com.demo.contaBancaria.ContaBancariaModel();
        contaMock.setUsuario(titular);

        ChavePixModel chave = new ChavePixModel();
        chave.setValor("12345678900");
        chave.setTipoChave("CPF");
        chave.setConta(contaMock);

        when(service.buscarPorValor("12345678900")).thenReturn(Optional.of(chave));

        ResponseEntity<?> response = controller.buscarPorChave("12345678900");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = (java.util.Map<?, ?>) response.getBody();
        assertThat(body.get("nomeCompleto")).isEqualTo("João Silva");
        assertThat(body.get("chave")).isEqualTo("12345678900");
        assertThat(body.get("tipoChave")).isEqualTo("CPF");
    }

    @Test
    @DisplayName("buscarPorChave: deve retornar 404 quando chave não existe")
    void buscarPorChave_deveRetornar404QuandoChaveNaoExiste() {
        when(service.buscarPorValor("chaveinexistente")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.buscarPorChave("chaveinexistente");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================
    //  GET /api/chaves-pix/conta — buscarDadosConta
    // =========================================================

    @Test
    @DisplayName("buscarDadosConta: deve retornar 200 com dados da conta")
    void buscarDadosConta_deveRetornar200QuandoContaEncontrada() {
        var conta = new bizi.com.demo.contaBancaria.ContaBancariaModel();
        when(service.buscarDetalhesDaConta("12345678900")).thenReturn(conta);

        ResponseEntity<ChavePixApiResponse> response = controller.buscarDadosConta(usuarioLogado);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSucesso()).isTrue();
    }

    @Test
    @DisplayName("buscarDadosConta: deve retornar 404 quando conta não encontrada")
    void buscarDadosConta_deveRetornar404QuandoContaNaoEncontrada() {
        when(service.buscarDetalhesDaConta(anyString())).thenThrow(new RuntimeException("Conta não encontrada"));

        ResponseEntity<ChavePixApiResponse> response = controller.buscarDadosConta(usuarioLogado);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSucesso()).isFalse();
    }

    // =========================================================
    //  GET /api/chaves-pix/chaves — listar
    // =========================================================

    @Test
    @DisplayName("listar: deve retornar 200 com lista de chaves")
    void listar_deveRetornar200ComListaDeChaves() {
        when(service.buscarIdContaPorUsuario("12345678900")).thenReturn(1L);
        when(service.listarChavesPorConta(1L)).thenReturn(List.of(new ChavePixModel(), new ChavePixModel()));

        ResponseEntity<ChavePixApiResponse> response = controller.listar(usuarioLogado);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSucesso()).isTrue();

        @SuppressWarnings("unchecked")
        List<ChavePixModel> chaves = (List<ChavePixModel>) response.getBody().getDados();
        assertThat(chaves).hasSize(2);
    }

    @Test
    @DisplayName("listar: deve retornar 401 quando usuário não autenticado")
    void listar_deveRetornar401QuandoUsuarioNulo() {
        ResponseEntity<ChavePixApiResponse> response = controller.listar(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).isEqualTo("Usuário não autenticado.");
    }

    @Test
    @DisplayName("listar: deve retornar lista vazia quando conta não tem chaves")
    void listar_deveRetornarListaVaziaQuandoSemChaves() {
        when(service.buscarIdContaPorUsuario("12345678900")).thenReturn(1L);
        when(service.listarChavesPorConta(1L)).thenReturn(List.of());

        ResponseEntity<ChavePixApiResponse> response = controller.listar(usuarioLogado);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        List<ChavePixModel> chaves = (List<ChavePixModel>) response.getBody().getDados();
        assertThat(chaves).isEmpty();
    }

    // =========================================================
    //  DELETE /api/chaves-pix/{id} — excluir
    // =========================================================

    @Test
    @DisplayName("excluir: deve retornar 200 quando chave removida com sucesso")
    void excluir_deveRetornar200QuandoSucesso() {
        doNothing().when(service).removerChave(1L);

        ResponseEntity<ChavePixApiResponse> response = controller.excluir(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSucesso()).isTrue();
        assertThat(response.getBody().getMensagem()).isEqualTo("Chave removida com sucesso!");
    }

    @Test
    @DisplayName("excluir: deve retornar 404 quando chave não existe")
    void excluir_deveRetornar404QuandoChaveInexistente() {
        doThrow(new RuntimeException("Chave não encontrada")).when(service).removerChave(99L);

        ResponseEntity<ChavePixApiResponse> response = controller.excluir(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSucesso()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("inexistente");
    }
}