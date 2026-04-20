package bizi.com.demo.logAcesso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAcessoRepository extends JpaRepository<LogAcessoModel, Long> {

    // Todos os logs de um usuário
    List<LogAcessoModel> findByUsuarioIdOrderByDataHoraAcessoDesc(Long idUsuario);

    // Logs por tipo de ação (LOGIN, LOGOUT, etc.)
    List<LogAcessoModel> findByTipoAcao(String tipoAcao);

    // Logs de um usuário filtrados por tipo de ação
    List<LogAcessoModel> findByUsuarioIdAndTipoAcao(Long idUsuario, String tipoAcao);
}