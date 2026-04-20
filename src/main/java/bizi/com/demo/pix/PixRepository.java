package bizi.com.demo.pix;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PixRepository extends JpaRepository<PixModel, Long> {

    List<PixModel> findByContaDestino(Long contaDestino);

    @Query("SELECT p FROM PixModel p WHERE p.transacao.contaBancaria.id = :idContaOrigem")
    List<PixModel> findByContaOrigem(@Param("idContaOrigem") Long idContaOrigem);

    @Query("SELECT p FROM PixModel p WHERE p.transacao.contaBancaria.id = :idConta OR p.contaDestino = :idConta")
    List<PixModel> findByContaOrigemOrDestino(@Param("idConta") Long idConta);
}