package bizi.com.demo.transferencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaRepository extends JpaRepository<TransferenciaModel, Long> {
    
    /**
     * Busca transferências por conta de destino
     */
    List<TransferenciaModel> findByContaDestino(Long contaDestino);
    
    /**
     * Busca transferências enviadas por uma conta (origem)
     */
    @Query("SELECT t FROM TransferenciaModel t WHERE t.transacao.contaBancaria.id = :idContaOrigem")
    List<TransferenciaModel> findByContaOrigem(@Param("idContaOrigem") Long idContaOrigem);
    
    /**
     * Busca todas as transferências relacionadas a uma conta (enviadas ou recebidas)
     */
    @Query("SELECT t FROM TransferenciaModel t WHERE t.transacao.contaBancaria.id = :idConta OR t.contaDestino = :idConta")
    List<TransferenciaModel> findByContaOrigemOrDestino(@Param("idConta") Long idConta);
}