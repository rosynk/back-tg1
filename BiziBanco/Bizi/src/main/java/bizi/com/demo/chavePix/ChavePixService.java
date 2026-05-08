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
        // 1. Validar limite de 5 chaves usando o nome correto do campo no repositório
        long totalChaves = repository.countByContaId(contaId);
        if (totalChaves >= 5) {
            throw new RuntimeException("Limite de 5 chaves atingido para esta conta.");
        }

        ContaBancariaModel conta = contaService.buscarPorId(contaId);

        ChavePixModel novaChave = new ChavePixModel();
        novaChave.setConta(conta); // Use o campo que mapeia para 'id_numero_conta'
        novaChave.setTipoChave(tipo.name()); // Salva o nome do Enum (ex: CPF, EMAIL)
        novaChave.setDataCadastro(java.time.LocalDateTime.now());

        // 2. Lógica para Chave Aleatória
        if (tipo == TipoChave.ALEATORIA) {
            novaChave.setValor(java.util.UUID.randomUUID().toString());
        } else {
            novaChave.setValor(valor);
        }

        return repository.save(novaChave);
    }

    public Long buscarIdContaPorUsuario(Long idUsuario) {
        // Agora o Java vai reconhecer o contaRepository aqui
        return contaRepository.findByUsuario_Id(idUsuario)
                .map(ContaBancariaModel::getId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
    }

    public List<ChavePixModel> listarChavesPorConta(Long contaId) {
        // Usa o método que criamos no Repository para filtrar pelo ID da conta
        return repository.findByContaId(contaId);
    }

    public ContaBancariaModel buscarDetalhesDaConta(String cpf) {
        // 1. Reutiliza sua lógica existente para encontrar o ID da conta via CPF
        Long contaId = buscarIdContaPorUsuario(cpf);

        // 2. Busca os dados completos da conta no banco de dados
        // Retorna o objeto ContaModel ou lança uma exceção caso não encontre
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada para o usuário informado."));
    }

    @Transactional
    public void removerChave(Long id) {
        repository.deleteById(id);
    }

    public Long buscarIdContaPorUsuario(String cpf) {
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