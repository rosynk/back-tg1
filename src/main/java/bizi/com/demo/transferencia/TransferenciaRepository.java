package bizi.com.demo.transferencia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaRepository extends JpaRepository<TransferenciaModel, Long> {

    // 1. Busca envios pelo ID interno da conta (Origem)
    List<TransferenciaModel> findByTransacaoContaBancariaId(Long idConta);

    // 2. Busca recebimentos pelo ID interno da conta (Destino)
    List<TransferenciaModel> findByContaDestino(Long idConta);

    // 3. Busca personalizada para o extrato (Une Origem e Destino)
    @Query("SELECT t FROM TransferenciaModel t WHERE t.transacao.contaBancaria.id = :id OR t.contaDestino = :id")
    List<TransferenciaModel> findByContaOrigemOrDestino(@Param("id") Long id);

    // --- O QUE ESTAVA FALTANDO PARA A LOGÍSTICA REALISTA ---

    /**
     * Busca transferências filtrando pela agência e conta do destinatário.
     * Útil para validar se já houve transferências para um destino específico.
     */
    List<TransferenciaModel> findByAgenciaDestinoAndContaDestino(String agencia, Long conta);

    /**
     * Método padrão para manter compatibilidade com chamadas que buscam apenas pela origem.
     */
    default List<TransferenciaModel> findByContaOrigem(Long id) {
        return findByTransacaoContaBancariaId(id);
    }
}