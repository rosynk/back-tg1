package bizi.com.demo.chavePix;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.usuario.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChavePixService {

    @Autowired
    private ChavePixRepository repository;

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private ContaBancariaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public ChavePixModel cadastrarChave(Long contaId, TipoChave tipo, String valor) {
        // 1. Validar limite de 5 chaves usando o nome correto do campo (contaBancaria)
        long totalChaves = repository.countByContaBancariaId(contaId);
        if (totalChaves >= 5) {
            throw new RuntimeException("Limite de 5 chaves atingido para esta conta.");
        }

        ContaBancariaModel conta = contaService.buscarPorId(contaId);

        ChavePixModel novaChave = new ChavePixModel();
        
        // CORREÇÃO: Usando o setter atualizado da Model
        novaChave.setConta(conta); 
        novaChave.setTipoChave(tipo.name()); 
        novaChave.setDataCadastro(LocalDateTime.now());

        // 2. Lógica para Chave Aleatória
        if (tipo == TipoChave.ALEATORIA) {
            novaChave.setValor(UUID.randomUUID().toString());
        } else {
            novaChave.setValor(valor);
        }

        return repository.save(novaChave);
    }

    public Long buscarIdContaPorUsuario(Long idUsuario) {
        return contaRepository.findByUsuario_Id(idUsuario)
                .map(ContaBancariaModel::getId)
                .orElse(null); // Retorna null em vez de quebrar o servidor
    }

    public List<ChavePixModel> listarChavesPorConta(Long contaId) {
        // Busca a lista de chaves filtrando pelo ID da conta bancária
        return repository.findByContaBancariaId(contaId);
    }

    public ContaBancariaModel buscarDetalhesDaConta(String cpf) {
        // 1. Reutiliza a lógica para encontrar o ID da conta via CPF
        Long contaId = buscarIdContaPorUsuario(cpf);

        // 2. Busca os dados completos da conta no banco de dados
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada para o usuário informado."));
    }

    @Transactional
    public void removerChave(Long id) {
        repository.deleteById(id);
    }

    public Long buscarIdContaPorUsuario(String cpf) {
        // Lógica de busca de conta através do CPF do usuário (Útil para o Onboarding)
        return usuarioRepository.findByCpf(cpf)
                .map(usuario -> {
                    if (usuario.getContas() != null && !usuario.getContas().isEmpty()) {
                        // Pega a primeira conta da lista (índice 0)
                        return usuario.getContas().get(0).getId();
                    }
                    throw new RuntimeException("O usuário não possui nenhuma conta bancária vinculada.");
                })
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o CPF: " + cpf));
    }
}