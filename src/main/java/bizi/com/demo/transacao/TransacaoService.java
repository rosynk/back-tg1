package bizi.com.demo.transacao;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private String getEmailLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isUsuarioAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional
    public TransacaoModel criarTransacao(TransacaoDto dto) {
        ContaBancariaModel conta = contaBancariaRepository.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta bancária não encontrada"));

        if (!isUsuarioAdmin() && !conta.getUsuario().getEmail().equals(getEmailLogado())) {
            throw new AccessDeniedException("Você não tem permissão para criar transações nesta conta.");
        }

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        
        // CORREÇÃO: Se o DTO envia String, convertemos para o Enum
        if (dto.getTipoTransacao() != null) {
            transacao.setTipoTransacao(TipoTransacao.valueOf(dto.getTipoTransacao().toUpperCase()));
        }
        
        transacao.setValor(dto.getValor());
        transacao.setDataHora(LocalDateTime.now());

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public TransacaoModel atualizarTransacao(Long id, TransacaoDto dto) {
        if (!isUsuarioAdmin()) {
            throw new AccessDeniedException("Transações bancárias não podem ser editadas por clientes.");
        }
        
        TransacaoModel transacao = buscarPorId(id);
        
        // CORREÇÃO: Conversão de String para Enum aqui também
        if (dto.getTipoTransacao() != null) {
            transacao.setTipoTransacao(TipoTransacao.valueOf(dto.getTipoTransacao().toUpperCase()));
        }
        
        if (dto.getValor() != null) transacao.setValor(dto.getValor());
        
        return transacaoRepository.save(transacao);
    }

    @Transactional
    public TransacaoModel registrarDebito(ContaBancariaModel conta, BigDecimal valor) {
        // 1. Atualiza o saldo da conta
        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);
        conta.setSaldo(novoSaldo);
        contaBancariaRepository.save(conta);

        // 2. Cria o registro no extrato
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setValor(valor);
        
        // Aqui já usamos o Enum diretamente, pois o registrarDebito é interno
        transacao.setTipoTransacao(TipoTransacao.PIX_SAIDA); 
        transacao.setDataHora(LocalDateTime.now());

        return transacaoRepository.save(transacao);
    }

    // --- Métodos de busca permanecem iguais ---
    
    public TransacaoModel buscarPorId(Long id) {
        TransacaoModel transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNotFoundException("Transação não encontrada"));
        if (!isUsuarioAdmin() && !transacao.getContaBancaria().getUsuario().getEmail().equals(getEmailLogado())) {
            throw new AccessDeniedException("Acesso negado a esta transação.");
        }
        return transacao;
    }

    public List<TransacaoModel> buscarPorConta(Long idConta) {
        ContaBancariaModel conta = contaBancariaRepository.findById(idConta)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não encontrada"));
        if (!isUsuarioAdmin() && !conta.getUsuario().getEmail().equals(getEmailLogado())) {
            throw new AccessDeniedException("Você só pode ver transações das suas próprias contas.");
        }
        return transacaoRepository.findByContaBancariaId(idConta);
    }

    public List<TransacaoModel> listarTodas() {
        if (!isUsuarioAdmin()) {
            throw new AccessDeniedException("Apenas administradores podem listar todas as transações do banco.");
        }
        return transacaoRepository.findAll();
    }

    @Transactional
    public void deletarTransacao(Long id) {
        if (!isUsuarioAdmin()) {
            throw new AccessDeniedException("Proibido excluir registros do histórico bancário.");
        }
        TransacaoModel transacao = buscarPorId(id);
        transacaoRepository.delete(transacao);
    }
    
    @Transactional
    public TransacaoModel realizarSaque(BigDecimal valor) {
        // 1. Pega o e-mail do usuário que está logado no sistema
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Busca a conta desse usuário
        ContaBancariaModel conta = contaBancariaRepository.findAll().stream()
                .filter(c -> c.getUsuario().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Conta não encontrada para o usuário logado."));

        // 3. Validação de Saldo
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente para realizar o saque.");
        }

        // 4. Atualiza o saldo da conta no banco
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaBancariaRepository.save(conta);

        // 5. Registra o evento na tabela de transações
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setValor(valor);
        transacao.setTipoTransacao(TipoTransacao.SAQUE); // O Enum que já criamos
        transacao.setDataHora(LocalDateTime.now());

        return transacaoRepository.save(transacao);
    }
    
    @Transactional
    public void realizarDeposito(BigDecimal valor) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ContaBancariaModel conta = contaBancariaRepository.findAll().stream()
                .filter(c -> c.getUsuario().getEmail().equals(email))
                .findFirst().orElseThrow();

        conta.setSaldo(conta.getSaldo().add(valor));
        contaBancariaRepository.save(conta);

        TransacaoModel t = new TransacaoModel();
        t.setContaBancaria(conta);
        t.setValor(valor);
        t.setTipoTransacao(TipoTransacao.DEPOSITO);
        t.setDataHora(LocalDateTime.now());
        transacaoRepository.save(t);
    }
}