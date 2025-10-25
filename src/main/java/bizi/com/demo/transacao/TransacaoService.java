package bizi.com.demo.transacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Transactional
    public TransacaoModel criarTransacao(TransacaoDto dto) {
        ContaBancariaModel conta = contaBancariaRepository.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta bancária não encontrada"));

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setTipoTransacao(dto.getTipoTransacao());
        transacao.setValor(dto.getValor());
        transacao.setDataHora(LocalDateTime.now());

        return transacaoRepository.save(transacao);
    }

    /**
     * Busca uma transação pelo ID
     */
    public TransacaoModel buscarPorId(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNotFoundException("Transação não encontrada"));
    }

    /**
     * Busca transações por conta bancária
     */
    public List<TransacaoModel> buscarPorConta(Long idConta) {
        return transacaoRepository.findByContaBancariaId(idConta);
    }

    /**
     * Busca transações por tipo
     */
    public List<TransacaoModel> buscarPorTipo(String tipoTransacao) {
        return transacaoRepository.findByTipoTransacao(tipoTransacao);
    }

    /**
     * Busca transações por conta e tipo
     */
    public List<TransacaoModel> buscarPorContaETipo(Long idConta, String tipoTransacao) {
        return transacaoRepository.findByContaBancariaIdAndTipoTransacao(idConta, tipoTransacao);
    }

    /**
     * Lista todas as transações
     */
    public List<TransacaoModel> listarTodas() {
        return transacaoRepository.findAll();
    }

    /**
     * Atualiza uma transação
     */
    @Transactional
    public TransacaoModel atualizarTransacao(Long id, TransacaoDto dto) {
        TransacaoModel transacao = buscarPorId(id);

        if (dto.getIdConta() != null && !dto.getIdConta().equals(transacao.getContaBancaria().getId())) {
            ContaBancariaModel conta = contaBancariaRepository.findById(dto.getIdConta())
                    .orElseThrow(() -> new ContaBancariaNotFoundException("Conta bancária não encontrada"));
            transacao.setContaBancaria(conta);
        }

        if (dto.getTipoTransacao() != null) {
            transacao.setTipoTransacao(dto.getTipoTransacao());
        }

        if (dto.getValor() != null) {
            transacao.setValor(dto.getValor());
        }

        return transacaoRepository.save(transacao);
    }

    /**
     * Deleta uma transação
     */
    @Transactional
    public void deletarTransacao(Long id) {
        TransacaoModel transacao = buscarPorId(id);
        transacaoRepository.delete(transacao);
    }
}