package bizi.com.demo.contaBancaria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.usuario.UsuarioModel;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancariaModel, Long> {

    // 1. Essencial para o PixService (Localizar conta pelo objeto Usuario)
    Optional<ContaBancariaModel> findByUsuario(UsuarioModel usuario);

    // 2. Essencial para o TransferenciaService (Logística de "Ida e Volta")
    // Permite encontrar o destino usando os dados que aparecem no cartão/app
    Optional<ContaBancariaModel> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);

    // 3. Essencial para a Segurança (Identificar conta do dono pelo e-mail do Token
    // JWT)
    List<ContaBancariaModel> findByUsuarioEmail(String email);

    // --- Seus métodos de consulta e filtros ---
    List<ContaBancariaModel> findByUsuarioId(Long idUsuario);

    List<ContaBancariaModel> findByNumeroAgencia(String numeroAgencia);

    List<ContaBancariaModel> findByStatusConta(Boolean statusConta);

    List<ContaBancariaModel> findByUsuarioIdAndStatusConta(Long idUsuario, Boolean statusConta);

    Optional<ContaBancariaModel> findByUsuarioCpf(String cpf);

    Optional<ContaBancariaModel> findByUsuario_Id(Long usuarioId);

}