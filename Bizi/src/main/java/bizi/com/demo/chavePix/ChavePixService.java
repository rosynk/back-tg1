package bizi.com.demo.chavePix;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public ChavePixModel cadastrarChave(Long contaId, TipoChave tipo, UsuarioModel usuario) {
        long totalChaves = repository.countByContaBancariaId(contaId);
        if (totalChaves >= 5) {
            throw new RuntimeException("Limite de 5 chaves atingido.");
        }

        String valor = switch (tipo) {
            case CPF       -> usuario.getCpf();
            case EMAIL     -> usuario.getEmail();
            case TELEFONE  -> usuario.getTelefone();
            case ALEATORIA -> UUID.randomUUID().toString();
        };

        // ← Verifica se já existe essa chave para essa conta
        boolean jaExiste = repository.existsByContaBancariaIdAndValor(contaId, valor);
        if (jaExiste) {
            throw new RuntimeException("Você já possui uma chave " + tipo.name() + " cadastrada.");
        }

        ContaBancariaModel conta = contaService.buscarPorId(contaId);
        ChavePixModel novaChave = new ChavePixModel();
        novaChave.setConta(conta);
        novaChave.setTipoChave(tipo.name());
        novaChave.setDataCadastro(LocalDateTime.now());
        novaChave.setValor(valor);

        return repository.save(novaChave);
    }
    
    public Optional<ChavePixModel> buscarPorValor(String valor) {
        return repository.findByValor(valor);
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