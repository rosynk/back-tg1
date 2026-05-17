package bizi.com.demo.chavePix;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChavePixRepository extends JpaRepository<ChavePixModel, Long> {

    Optional<ChavePixModel> findByValor(String valor);

    boolean existsByValor(String valor);

    List<ChavePixModel> findByContaBancaria(ContaBancariaModel contaBancaria);

    List<ChavePixModel> findByContaBancariaId(Long idConta);

    @Query("SELECT COUNT(c) FROM ChavePixModel c WHERE c.contaBancaria.id = :idConta")
    long countByContaBancariaId(@Param("idConta") Long idConta);
    
    boolean existsByContaBancariaIdAndValor(Long contaBancariaId, String valor);

    List<ChavePixModel> findByContaBancaria_Id(Long id);
}