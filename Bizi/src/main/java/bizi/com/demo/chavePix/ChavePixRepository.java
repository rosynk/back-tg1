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

    // Busca todas as chaves associadas ao objeto ContaBancariaModel
    // CORREÇÃO: O nome do campo na Model agora é 'contaBancaria'
    List<ChavePixModel> findByContaBancaria(ContaBancariaModel contaBancaria);

    // Busca todas as chaves associadas ao ID de uma conta
    // O Spring agora segue o caminho: contaBancaria -> id
    List<ChavePixModel> findByContaBancariaId(Long idConta);

    // Query customizada atualizada para o novo nome do atributo
    @Query("SELECT COUNT(c) FROM ChavePixModel c WHERE c.contaBancaria.id = :idConta")
    long countByContaBancariaId(@Param("idConta") Long idConta);

    /**
     * Busca pelo ID da conta usando a convenção de underscore (opcional)
     * Útil se houver ambiguidade, mas o findByContaBancariaId já resolve.
     */
    List<ChavePixModel> findByContaBancaria_Id(Long id);
}