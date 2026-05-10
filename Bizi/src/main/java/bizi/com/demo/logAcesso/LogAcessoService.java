package bizi.com.demo.logAcesso;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioNotFoundException;
import bizi.com.demo.usuario.UsuarioRepository;

@Service
public class LogAcessoService {

    @Autowired
    private LogAcessoRepository logAcessoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registra um novo log de acesso.
     * Chamado internamente (ex: no login/logout) ou via endpoint.
     */
    @Transactional
    public LogAcessoModel registrarLog(LogAcessoDto dto) {
        UsuarioModel usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException(
                        "Usuário não encontrado com ID: " + dto.getIdUsuario()));

        LogAcessoModel log = new LogAcessoModel();
        log.setUsuario(usuario);
        log.setDataHoraAcesso(LocalDateTime.now());
        log.setIpOrigem(dto.getIpOrigem());
        log.setTipoAcao(dto.getTipoAcao());

        return logAcessoRepository.save(log);
    }

    /**
     * Busca todos os logs de um usuário, do mais recente ao mais antigo.
     */
    @Transactional(readOnly = true)
    public List<LogAcessoModel> buscarPorUsuario(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new UsuarioNotFoundException("Usuário não encontrado com ID: " + idUsuario);
        }
        return logAcessoRepository.findByUsuarioIdOrderByDataHoraAcessoDesc(idUsuario);
    }

    /**
     * Busca logs por tipo de ação (ex: LOGIN, LOGOUT, TRANSFERENCIA).
     */
    @Transactional(readOnly = true)
    public List<LogAcessoModel> buscarPorTipoAcao(String tipoAcao) {
        return logAcessoRepository.findByTipoAcao(tipoAcao);
    }

    /**
     * Busca logs de um usuário filtrados por tipo de ação.
     */
    @Transactional(readOnly = true)
    public List<LogAcessoModel> buscarPorUsuarioETipo(Long idUsuario, String tipoAcao) {
        return logAcessoRepository.findByUsuarioIdAndTipoAcao(idUsuario, tipoAcao);
    }

    /**
     * Busca um log pelo ID.
     */
    @Transactional(readOnly = true)
    public LogAcessoModel buscarPorId(Long id) {
        return logAcessoRepository.findById(id)
                .orElseThrow(() -> new LogAcessoNotFoundException("Log não encontrado com ID: " + id));
    }
}