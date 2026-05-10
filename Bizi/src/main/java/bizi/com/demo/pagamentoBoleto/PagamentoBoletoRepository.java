package bizi.com.demo.pagamentoBoleto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoBoletoRepository extends JpaRepository<PagamentoBoletoModel, Long> {

    @Query("SELECT p FROM PagamentoBoletoModel p WHERE p.transacao.contaBancaria.id = :idConta")
    List<PagamentoBoletoModel> findByContaOrigem(@Param("idConta") Long idConta);

    
    List<PagamentoBoletoModel> findByTransacao_ContaBancaria_Id(Long contaId);
    
    
    List<PagamentoBoletoModel> findByCodigoBarras(String codigoBarras);
}