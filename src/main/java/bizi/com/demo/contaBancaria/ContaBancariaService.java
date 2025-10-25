package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioNotFoundException;
import bizi.com.demo.usuario.UsuarioRepository;

@Service
public class ContaBancariaService {

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Cria uma nova conta bancária
     */
    @Transactional
    public ContaBancariaModel criarConta(ContaBancariaDto dto) {
        UsuarioModel usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        ContaBancariaModel conta = new ContaBancariaModel();
        conta.setUsuario(usuario);
        conta.setNumeroAgencia(dto.getNumeroAgencia());
        conta.setTipoConta(dto.getTipoConta());
        conta.setStatusConta(true);
        conta.setSaldo(dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO);
        conta.setDataCriacao(LocalDateTime.now());

        return contaBancariaRepository.save(conta);
    }

    /**
     * Busca uma conta pelo ID
     */
    public ContaBancariaModel buscarPorId(Long id) {
        return contaBancariaRepository.findById(id)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não encontrada"));
    }

    /**
     * Busca contas por usuário
     */
    public List<ContaBancariaModel> buscarPorUsuario(Long idUsuario) {
        return contaBancariaRepository.findByUsuarioId(idUsuario);
    }

    /**
     * Busca contas por agência
     */
    public List<ContaBancariaModel> buscarPorAgencia(String numeroAgencia) {
        return contaBancariaRepository.findByNumeroAgencia(numeroAgencia);
    }

    /**
     * Lista todas as contas
     */
    public List<ContaBancariaModel> listarTodas() {
        return contaBancariaRepository.findAll();
    }

    /**
     * Atualiza uma conta bancária
     */
    @Transactional
    public ContaBancariaModel atualizarConta(Long id, ContaBancariaDto dto) {
        ContaBancariaModel conta = buscarPorId(id);

        if (dto.getIdUsuario() != null && !dto.getIdUsuario().equals(conta.getUsuario().getId())) {
            UsuarioModel usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));
            conta.setUsuario(usuario);
        }

        if (dto.getNumeroAgencia() != null) {
            conta.setNumeroAgencia(dto.getNumeroAgencia());
        }

        if (dto.getTipoConta() != null) {
            conta.setTipoConta(dto.getTipoConta());
        }

        if (dto.getSaldo() != null) {
            conta.setSaldo(dto.getSaldo());
        }

        return contaBancariaRepository.save(conta);
    }

    /**
     * Altera o status da conta
     */
    @Transactional
    public ContaBancariaModel alterarStatus(Long id, Boolean novoStatus) {
        ContaBancariaModel conta = buscarPorId(id);
        conta.setStatusConta(novoStatus);
        return contaBancariaRepository.save(conta);
    }

    /**
     * Deleta uma conta
     */
    @Transactional
    public void deletarConta(Long id) {
        ContaBancariaModel conta = buscarPorId(id);
        contaBancariaRepository.delete(conta);
    }
}