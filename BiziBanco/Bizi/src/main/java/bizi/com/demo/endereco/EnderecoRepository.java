package bizi.com.demo.endereco;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository extends JpaRepository<EnderecoModel, Long> {
    
    /**
     * Busca endereços por CEP
     * @param cep CEP do endereço
     * @return Lista de endereços com o CEP informado
     */
    List<EnderecoModel> findByCep(String cep);
    
    /**
     * Busca endereços por cidade
     * @param cidade Nome da cidade
     * @return Lista de endereços na cidade informada
     */
    List<EnderecoModel> findByCidade(String cidade);
    
    /**
     * Busca endereços por estado
     * @param estado Sigla do estado
     * @return Lista de endereços no estado informado
     */
    List<EnderecoModel> findByEstado(String estado);
    
    /**
     * Busca endereços por bairro
     * @param bairro Nome do bairro
     * @return Lista de endereços no bairro informado
     */
    List<EnderecoModel> findByBairro(String bairro);
    
    /**
     * Busca endereços por cidade e estado
     * @param cidade Nome da cidade
     * @param estado Sigla do estado
     * @return Lista de endereços na cidade e estado informados
     */
    List<EnderecoModel> findByCidadeAndEstado(String cidade, String estado);
    
    /**
     * Verifica se existe endereço com o CEP informado
     * @param cep CEP do endereço
     * @return true se existe, false caso contrário
     */
    boolean existsByCep(String cep);
}