package bizi.com.demo.logAcesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioNotFoundException;
import bizi.com.demo.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class LogAcessoServiceTest {

    @Mock private LogAcessoRepository logAcessoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LogAcessoService service;

    private UsuarioModel usuario;
    private LogAcessoModel log;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioModel();
        usuario.setId(1L);
        usuario.setCpf("12345678900");

        log = new LogAcessoModel();
        log.setId(10L);
        log.setUsuario(usuario);
        log.setIpOrigem("192.168.0.1");
        log.setTipoAcao("LOGIN");
    }

    // =========================================================
    //  registrarLog
    // =========================================================

    @Test
    @DisplayName("registrarLog: deve registrar log vinculando usuário pelo ID do DTO")
    void registrarLog_deveVincularUsuarioPeloIdDoDto() {
        LogAcessoDto dto = new LogAcessoDto();
        dto.setIdUsuario(1L);
        dto.setIpOrigem("192.168.0.1");
        dto.setTipoAcao("LOGIN");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(logAcessoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LogAcessoModel resultado = service.registrarLog(dto);

        assertThat(resultado.getUsuario()).isEqualTo(usuario);
        assertThat(resultado.getIpOrigem()).isEqualTo("192.168.0.1");
        assertThat(resultado.getTipoAcao()).isEqualTo("LOGIN");
        assertThat(resultado.getDataHoraAcesso()).isNotNull();
        verify(logAcessoRepository).save(any());
    }

    @Test
    @DisplayName("registrarLog: deve preencher dataHoraAcesso automaticamente")
    void registrarLog_devePreencherDataHoraAcessoAutomaticamente() {
        LogAcessoDto dto = new LogAcessoDto();
        dto.setIdUsuario(1L);
        dto.setTipoAcao("LOGOUT");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(logAcessoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LogAcessoModel resultado = service.registrarLog(dto);

        assertThat(resultado.getDataHoraAcesso()).isNotNull();
    }

    @Test
    @DisplayName("registrarLog: deve lançar exceção quando usuário não encontrado")
    void registrarLog_deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        LogAcessoDto dto = new LogAcessoDto();
        dto.setIdUsuario(999L);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarLog(dto))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining("999");

        verify(logAcessoRepository, never()).save(any());
    }

    // =========================================================
    //  buscarPorUsuario
    // =========================================================

    @Test
    @DisplayName("buscarPorUsuario: deve retornar lista de logs do usuário")
    void buscarPorUsuario_deveRetornarListaDeLogs() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(logAcessoRepository.findByUsuarioIdOrderByDataHoraAcessoDesc(1L))
                .thenReturn(List.of(log));

        List<LogAcessoModel> resultado = service.buscarPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(log);
    }

    @Test
    @DisplayName("buscarPorUsuario: deve retornar lista vazia quando usuário não tem logs")
    void buscarPorUsuario_deveRetornarListaVaziaQuandoSemLogs() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(logAcessoRepository.findByUsuarioIdOrderByDataHoraAcessoDesc(1L))
                .thenReturn(List.of());

        List<LogAcessoModel> resultado = service.buscarPorUsuario(1L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarPorUsuario: deve lançar exceção quando usuário não existe")
    void buscarPorUsuario_deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.buscarPorUsuario(999L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining("999");

        verify(logAcessoRepository, never()).findByUsuarioIdOrderByDataHoraAcessoDesc(any());
    }

    // =========================================================
    //  buscarPorTipoAcao
    // =========================================================

    @Test
    @DisplayName("buscarPorTipoAcao: deve retornar logs filtrados pelo tipo")
    void buscarPorTipoAcao_deveRetornarLogsFiltradosPeloTipo() {
        LogAcessoModel logLogout = new LogAcessoModel();
        logLogout.setTipoAcao("LOGOUT");

        when(logAcessoRepository.findByTipoAcao("LOGOUT")).thenReturn(List.of(logLogout));

        List<LogAcessoModel> resultado = service.buscarPorTipoAcao("LOGOUT");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoAcao()).isEqualTo("LOGOUT");
    }

    @Test
    @DisplayName("buscarPorTipoAcao: deve retornar lista vazia quando nenhum log encontrado")
    void buscarPorTipoAcao_deveRetornarVazioQuandoNenhumLogEncontrado() {
        when(logAcessoRepository.findByTipoAcao("PIX")).thenReturn(List.of());

        List<LogAcessoModel> resultado = service.buscarPorTipoAcao("PIX");

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    //  buscarPorUsuarioETipo
    // =========================================================

    @Test
    @DisplayName("buscarPorUsuarioETipo: deve retornar logs do usuário filtrados por tipo")
    void buscarPorUsuarioETipo_deveRetornarLogsFiltrados() {
        when(logAcessoRepository.findByUsuarioIdAndTipoAcao(1L, "LOGIN"))
                .thenReturn(List.of(log));

        List<LogAcessoModel> resultado = service.buscarPorUsuarioETipo(1L, "LOGIN");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoAcao()).isEqualTo("LOGIN");
    }

    @Test
    @DisplayName("buscarPorUsuarioETipo: deve retornar vazio quando combinação não encontrada")
    void buscarPorUsuarioETipo_deveRetornarVazioQuandoNaoEncontrado() {
        when(logAcessoRepository.findByUsuarioIdAndTipoAcao(1L, "TRANSFERENCIA"))
                .thenReturn(List.of());

        List<LogAcessoModel> resultado = service.buscarPorUsuarioETipo(1L, "TRANSFERENCIA");

        assertThat(resultado).isEmpty();
    }

    // =========================================================
    //  buscarPorId
    // =========================================================

    @Test
    @DisplayName("buscarPorId: deve retornar log quando encontrado")
    void buscarPorId_deveRetornarLogQuandoEncontrado() {
        when(logAcessoRepository.findById(10L)).thenReturn(Optional.of(log));

        LogAcessoModel resultado = service.buscarPorId(10L);

        assertThat(resultado).isEqualTo(log);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando log não encontrado")
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
        when(logAcessoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(LogAcessoNotFoundException.class)
                .hasMessageContaining("999");
    }
}