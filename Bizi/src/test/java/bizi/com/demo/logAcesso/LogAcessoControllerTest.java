package bizi.com.demo.logAcesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioNotFoundException;

@ExtendWith(MockitoExtension.class)
class LogAcessoControllerTest {

    @Mock private LogAcessoService logAcessoService;

    @InjectMocks
    private LogAcessoController controller;

    private LogAcessoModel log;

    @BeforeEach
    void setUp() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setId(1L);

        log = new LogAcessoModel();
        log.setId(10L);
        log.setUsuario(usuario);
        log.setIpOrigem("192.168.0.1");
        log.setTipoAcao("LOGIN");
    }

    // =========================================================
    //  POST /api/logs-acesso — registrarLog
    // =========================================================

    @Test
    @DisplayName("registrarLog: deve retornar 201 quando log registrado com sucesso")
    void registrarLog_deveRetornar201QuandoSucesso() {
        LogAcessoDto dto = new LogAcessoDto();
        dto.setIdUsuario(1L);
        dto.setTipoAcao("LOGIN");

        when(logAcessoService.registrarLog(dto)).thenReturn(log);

        ResponseEntity<?> response = controller.registrarLog(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(log);
    }

    @Test
    @DisplayName("registrarLog: deve retornar 404 quando usuário não encontrado")
    void registrarLog_deveRetornar404QuandoUsuarioNaoEncontrado() {
        LogAcessoDto dto = new LogAcessoDto();
        dto.setIdUsuario(999L);

        when(logAcessoService.registrarLog(dto))
                .thenThrow(new UsuarioNotFoundException("Usuário não encontrado com ID: 999"));

        ResponseEntity<?> response = controller.registrarLog(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("registrarLog: deve retornar 500 quando ocorre erro inesperado")
    void registrarLog_deveRetornar500QuandoErroInesperado() {
        LogAcessoDto dto = new LogAcessoDto();
        dto.setIdUsuario(1L);

        when(logAcessoService.registrarLog(dto))
                .thenThrow(new RuntimeException("Erro inesperado"));

        ResponseEntity<?> response = controller.registrarLog(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).contains("Erro ao registrar log");
    }

    // =========================================================
    //  GET /api/logs-acesso/{id} — buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar 200 com log quando encontrado")
    void buscarPorId_deveRetornar200QuandoEncontrado() {
        when(logAcessoService.buscarPorId(10L)).thenReturn(log);

        ResponseEntity<LogAcessoModel> response = controller.buscarPorId(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(log);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar 404 quando log não encontrado")
    void buscarPorId_deveRetornar404QuandoNaoEncontrado() {
        when(logAcessoService.buscarPorId(999L))
                .thenThrow(new LogAcessoNotFoundException("Log não encontrado com ID: 999"));

        ResponseEntity<LogAcessoModel> response = controller.buscarPorId(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================
    //  GET /api/logs-acesso/usuario/{idUsuario} — buscarPorUsuario
    // =========================================================

    @Test
    @DisplayName("buscarPorUsuario: deve retornar 200 com lista de logs do usuário")
    void buscarPorUsuario_deveRetornar200ComListaDeLogs() {
        when(logAcessoService.buscarPorUsuario(1L)).thenReturn(List.of(log));

        ResponseEntity<?> response = controller.buscarPorUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorUsuario: deve retornar 200 com lista vazia quando usuário não tem logs")
    void buscarPorUsuario_deveRetornar200ComListaVazia() {
        when(logAcessoService.buscarPorUsuario(1L)).thenReturn(List.of());

        ResponseEntity<?> response = controller.buscarPorUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("buscarPorUsuario: deve retornar 404 quando usuário não existe")
    void buscarPorUsuario_deveRetornar404QuandoUsuarioNaoExiste() {
        when(logAcessoService.buscarPorUsuario(999L))
                .thenThrow(new UsuarioNotFoundException("Usuário não encontrado com ID: 999"));

        ResponseEntity<?> response = controller.buscarPorUsuario(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================
    //  GET /api/logs-acesso/usuario/{idUsuario}/tipo — buscarPorUsuarioETipo
    // =========================================================

    @Test
    @DisplayName("buscarPorUsuarioETipo: deve retornar 200 com logs filtrados por tipo")
    void buscarPorUsuarioETipo_deveRetornar200ComLogsFiltrados() {
        when(logAcessoService.buscarPorUsuarioETipo(1L, "LOGIN")).thenReturn(List.of(log));

        ResponseEntity<List<LogAcessoModel>> response = controller.buscarPorUsuarioETipo(1L, "LOGIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTipoAcao()).isEqualTo("LOGIN");
    }

    @Test
    @DisplayName("buscarPorUsuarioETipo: deve retornar 200 com lista vazia quando não há logs do tipo")
    void buscarPorUsuarioETipo_deveRetornar200VazioQuandoSemLogs() {
        when(logAcessoService.buscarPorUsuarioETipo(anyLong(), anyString())).thenReturn(List.of());

        ResponseEntity<List<LogAcessoModel>> response = controller.buscarPorUsuarioETipo(1L, "PIX");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // =========================================================
    //  GET /api/logs-acesso/tipo/{tipoAcao} — buscarPorTipoAcao
    // =========================================================

    @Test
    @DisplayName("buscarPorTipoAcao: deve retornar 200 com todos os logs do tipo")
    void buscarPorTipoAcao_deveRetornar200ComTodosOsLogs() {
        LogAcessoModel logLogout = new LogAcessoModel();
        logLogout.setTipoAcao("LOGOUT");

        when(logAcessoService.buscarPorTipoAcao("LOGOUT")).thenReturn(List.of(logLogout));

        ResponseEntity<List<LogAcessoModel>> response = controller.buscarPorTipoAcao("LOGOUT");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTipoAcao()).isEqualTo("LOGOUT");
    }

    @Test
    @DisplayName("buscarPorTipoAcao: deve retornar 200 com lista vazia quando nenhum log encontrado")
    void buscarPorTipoAcao_deveRetornar200VazioQuandoNenhumLog() {
        when(logAcessoService.buscarPorTipoAcao("TRANSFERENCIA")).thenReturn(List.of());

        ResponseEntity<List<LogAcessoModel>> response = controller.buscarPorTipoAcao("TRANSFERENCIA");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}