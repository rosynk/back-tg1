package bizi.com.demo.contaBancaria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancariaModel, Long> {
    
    /**
     * Busca todas as contas de um usuário
     */
    List<ContaBancariaModel> findByUsuarioId(Long idUsuario);
    
    /**
     * Busca contas por número de agência
     */
    List<ContaBancariaModel> findByNumeroAgencia(String numeroAgencia);
    
    /**
     * Busca contas ativas
     */
    List<ContaBancariaModel> findByStatusConta(Boolean statusConta);
    
    /**
     * Busca contas de um usuário com status específico
     */
    List<ContaBancariaModel> findByUsuarioIdAndStatusConta(Long idUsuario, Boolean statusConta);
}