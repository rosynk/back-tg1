package bizi.com.demo.endereco;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    /**
     * Cria um novo endereço
     * @param enderecoDto Dados do endereço
     * @return EnderecoModel criado
     */
    public EnderecoModel criarEndereco(EnderecoDto enderecoDto) {
        EnderecoModel endereco = new EnderecoModel();
        endereco.setRua(enderecoDto.getRua());
        endereco.setNumero(enderecoDto.getNumero());
        endereco.setComplemento(enderecoDto.getComplemento());
        endereco.setBairro(enderecoDto.getBairro());
        endereco.setCidade(enderecoDto.getCidade());
        endereco.setEstado(enderecoDto.getEstado());
        endereco.setCep(enderecoDto.getCep());
        
        return enderecoRepository.save(endereco);
    }

    /**
     * Busca um endereço pelo ID
     * @param id ID do endereço
     * @return EnderecoModel se encontrado
     */
    @Transactional(readOnly = true)
    public EnderecoModel buscarPorId(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException("Endereço não encontrado com ID: " + id));
    }

    /**
     * Lista todos os endereços
     * @return Lista de endereços
     */
    @Transactional(readOnly = true)
    public List<EnderecoModel> listarTodos() {
        return enderecoRepository.findAll();
    }

    /**
     * Busca endereços por CEP
     * @param cep CEP do endereço
     * @return Lista de endereços com o CEP informado
     */
    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorCep(String cep) {
        return enderecoRepository.findByCep(cep);
    }

    /**
     * Busca endereços por cidade
     * @param cidade Nome da cidade
     * @return Lista de endereços na cidade informada
     */
    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorCidade(String cidade) {
        return enderecoRepository.findByCidade(cidade);
    }

    /**
     * Busca endereços por estado
     * @param estado Sigla do estado
     * @return Lista de endereços no estado informado
     */
    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorEstado(String estado) {
        return enderecoRepository.findByEstado(estado);
    }

    /**
     * Busca endereços por bairro
     * @param bairro Nome do bairro
     * @return Lista de endereços no bairro informado
     */
    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorBairro(String bairro) {
        return enderecoRepository.findByBairro(bairro);
    }

    /**
     * Busca endereços por cidade e estado
     * @param cidade Nome da cidade
     * @param estado Sigla do estado
     * @return Lista de endereços na cidade e estado informados
     */
    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorCidadeEEstado(String cidade, String estado) {
        return enderecoRepository.findByCidadeAndEstado(cidade, estado);
    }

    /**
     * Atualiza um endereço existente
     * @param id ID do endereço
     * @param enderecoDto Dados atualizados
     * @return EnderecoModel atualizado
     */
    public EnderecoModel atualizarEndereco(Long id, EnderecoDto enderecoDto) {
        EnderecoModel enderecoExistente = buscarPorId(id);
        
        // Atualizar campos
        enderecoExistente.setRua(enderecoDto.getRua());
        enderecoExistente.setNumero(enderecoDto.getNumero());
        enderecoExistente.setComplemento(enderecoDto.getComplemento());
        enderecoExistente.setBairro(enderecoDto.getBairro());
        enderecoExistente.setCidade(enderecoDto.getCidade());
        enderecoExistente.setEstado(enderecoDto.getEstado());
        enderecoExistente.setCep(enderecoDto.getCep());
        
        return enderecoRepository.save(enderecoExistente);
    }

    /**
     * Deleta um endereço
     * @param id ID do endereço
     */
    public void deletarEndereco(Long id) {
        if (!enderecoRepository.existsById(id)) {
            throw new EnderecoNotFoundException("Endereço não encontrado com ID: " + id);
        }
        enderecoRepository.deleteById(id);
    }

    /**
     * Verifica se um endereço existe
     * @param id ID do endereço
     * @return true se existe, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean existeEndereco(Long id) {
        return enderecoRepository.existsById(id);
    }
}
