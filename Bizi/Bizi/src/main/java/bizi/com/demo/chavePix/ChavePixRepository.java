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

    // Busca uma chave pelo valor (Ex: CPF, Email, etc)
    Optional<ChavePixModel> findByValor(String valor);

    // Verifica se o valor da chave já existe no sistema
    boolean existsByValor(String valor);

    // Busca todas as chaves associadas a um objeto ContaBancariaModel
    List<ChavePixModel> findByConta(ContaBancariaModel conta);

    // Busca todas as chaves associadas ao ID de uma conta
    // O Spring entende que deve buscar o campo 'id' dentro do objeto 'conta'
    List<ChavePixModel> findByContaId(Long idConta);

    // Query customizada para contar chaves de uma conta específica
    @Query("SELECT COUNT(c) FROM ChavePixModel c WHERE c.conta.id = :idConta")
    long countByContaId(@Param("idConta") Long idConta);

    /**
     * Se você precisar buscar pelo ID que o usuário digita (o número da conta),
     * certifique-se de que o atributo dentro de ContaBancariaModel se chama
     * 'idNumeroConta'.
     * Caso contrário, use apenas o findByContaId acima.
     */
    List<ChavePixModel> findByConta_Id(Long id);

}