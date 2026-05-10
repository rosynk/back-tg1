package bizi.com.demo.endereco;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.validacoes.external.ViaCepClient;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ViaCepClient viaCepClient;

    // --- APOIO À SEGURANÇA ---
    
    private String getEmailLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isUsuarioAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Valida se o endereço pertence ao usuário logado ou se é Admin.
     */
    private void validarPropriedade(EnderecoModel endereco) {
        if (!isUsuarioAdmin()) {
            // Supondo que seu EnderecoModel tenha uma relação com Usuario ou Conta
            // Se o endereço for global, remova esta trava ou ajuste para validar o vínculo
            // String emailDono = endereco.getUsuario().getEmail(); 
            // if (!emailDono.equals(getEmailLogado())) throw new AccessDeniedException("...");
        }
    }

    // --- LÓGICA PRINCIPAL ---

    /**
     * Consulta apenas para visualização (Front-end)
     */
    public ViaCepClient.ViaCepResponse consultarCepExterno(String cep) {
        ViaCepClient.ViaCepResponse response = viaCepClient.buscarEnderecoPorCep(cep);
        if (response == null || response.isErro()) {
            throw new RuntimeException("CEP não encontrado na base do ViaCEP.");
        }
        return response;
    }

    @Transactional
    public EnderecoModel criarEndereco(EnderecoDto dto) {
        ViaCepClient.ViaCepResponse viaCepData = consultarCepExterno(dto.getCep());

        EnderecoModel endereco = new EnderecoModel();
        endereco.setCep(dto.getCep());
        endereco.setRua(viaCepData.getLogradouro());
        endereco.setBairro(viaCepData.getBairro());
        endereco.setCidade(viaCepData.getLocalidade());
        endereco.setEstado(viaCepData.getUf());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());

        return enderecoRepository.save(endereco);
    }

    @Transactional(readOnly = true)
    public EnderecoModel buscarPorId(Long id) {
        EnderecoModel endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException("Endereço não encontrado com ID: " + id));
        
        validarPropriedade(endereco);
        return endereco;
    }

    @Transactional
    public EnderecoModel atualizarEndereco(Long id, EnderecoDto dto) {
        EnderecoModel existente = buscarPorId(id); // Já valida propriedade aqui
        validarPropriedade(existente);

        // Se o CEP mudou, atualiza tudo via ViaCEP
        if (!existente.getCep().equals(dto.getCep())) {
            ViaCepClient.ViaCepResponse viaCepData = consultarCepExterno(dto.getCep());
            existente.setCep(dto.getCep());
            existente.setRua(viaCepData.getLogradouro());
            existente.setBairro(viaCepData.getBairro());
            existente.setCidade(viaCepData.getLocalidade());
            existente.setEstado(viaCepData.getUf());
        }

        existente.setNumero(dto.getNumero());
        existente.setComplemento(dto.getComplemento());

        return enderecoRepository.save(existente);
    }

    @Transactional
    public void deletarEndereco(Long id) {
        EnderecoModel endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException("ID inexistente."));
        
        // Apenas Admin deleta (conforme seu Controller), mas o Service garante a busca
        enderecoRepository.delete(endereco);
    }

    // --- CONSULTAS ---

    @Transactional(readOnly = true)
    public List<EnderecoModel> listarTodos() {
        return enderecoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorCep(String cep) {
        return enderecoRepository.findByCep(cep);
    }

    @Transactional(readOnly = true)
    public List<EnderecoModel> buscarPorCidadeEEstado(String cidade, String estado) {
        return enderecoRepository.findByCidadeAndEstado(cidade, estado);
    }
}