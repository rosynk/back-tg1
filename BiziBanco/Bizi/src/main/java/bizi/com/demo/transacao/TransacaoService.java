package bizi.com.demo.transacao;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.usuario.UsuarioModel;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    // --- APOIO: SEGURANÇA E CONTEXTO ---

    private UsuarioModel getUsuarioLogado() {
        return (UsuarioModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean isUsuarioAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validarPosseConta(ContaBancariaModel conta) {
        if (isUsuarioAdmin())
            return;
        String cpfLogado = getUsuarioLogado().getCpf();
        if (!cpfLogado.equals(conta.getUsuario().getCpf())) {
            throw new AccessDeniedException("Acesso Negado: CPF do titular não confere com o usuário logado.");
        }
    }

    // --- OPERAÇÕES FINANCEIRAS ---

    @Transactional
    public TransacaoModel criarTransacao(TransacaoDto dto) {
        // 1. Busca a conta bancária pelo ID fornecido no DTO
        ContaBancariaModel conta = contaBancariaRepository.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta bancária não encontrada."));

        // 2. Valida se o usuário logado (via Token) é o dono da conta
        validarPosseConta(conta);

        // 3. Instancia a transação e preenche os dados
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setValor(dto.getValor());
        transacao.setDataHora(LocalDateTime.now());

        // Converte o tipo enviado no JSON para o Enum (ex: "SAQUE" ->
        // TipoTransacao.SAQUE)
        if (dto.getTipoTransacao() != null) {
            transacao.setTipoTransacao(TipoTransacao.valueOf(dto.getTipoTransacao().toUpperCase()));
        }

        // Preenche o CPF de origem para o histórico do extrato
        transacao.setCpfOrigem(getUsuarioLogado().getCpf());

        // 4. Salva no banco de dados
        return transacaoRepository.save(transacao);
    }

    @Transactional
    public TransacaoModel realizarSaque(BigDecimal valor) {
        String cpf = getUsuarioLogado().getCpf();
        ContaBancariaModel conta = contaBancariaRepository.findByUsuarioCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Conta não localizada para o CPF logado."));

        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }

        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaBancariaRepository.save(conta);

        return salvarTransacao(conta, valor, TipoTransacao.SAQUE);
    }

    @Transactional
    public void realizarDeposito(BigDecimal valor) {
        String cpf = getUsuarioLogado().getCpf();
        ContaBancariaModel conta = contaBancariaRepository.findByUsuarioCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Conta não localizada para depósito."));

        conta.setSaldo(conta.getSaldo().add(valor));
        contaBancariaRepository.save(conta);

        salvarTransacao(conta, valor, TipoTransacao.DEPOSITO);
    }

    private TransacaoModel salvarTransacao(ContaBancariaModel conta, BigDecimal valor, TipoTransacao tipo) {
        TransacaoModel t = new TransacaoModel();
        t.setContaBancaria(conta);
        t.setValor(valor);
        t.setTipoTransacao(tipo);
        t.setDataHora(LocalDateTime.now());
        // Se sua model tiver campos cpfOrigem/Destino, preencha-os aqui:
        t.setCpfOrigem(getUsuarioLogado().getCpf());
        return transacaoRepository.save(t);
    }

    // --- MÉTODOS DE BUSCA (EXTRATO E FILTROS) ---

    public List<TransacaoModel> listarExtratoCompleto() {
        // Busca todas as transações onde o usuário participou (CPF origem ou destino)
        return transacaoRepository.findByCpfParaExtrato(getUsuarioLogado().getCpf());
    }

    public List<TransacaoModel> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        String cpf = getUsuarioLogado().getCpf();
        // Filtra o extrato por data
        return transacaoRepository.findByCpfParaExtrato(cpf).stream()
                .filter(t -> t.getDataHora().isAfter(inicio) && t.getDataHora().isBefore(fim))
                .collect(Collectors.toList());
    }

    public List<TransacaoModel> buscarPorTipo(TipoTransacao tipo) {
        String cpf = getUsuarioLogado().getCpf();
        return transacaoRepository.findByCpfParaExtrato(cpf).stream()
                .filter(t -> t.getTipoTransacao().equals(tipo))
                .collect(Collectors.toList());
    }

    public TransacaoModel buscarPorId(Long id) {
        TransacaoModel transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNotFoundException("Transação " + id + " não encontrada."));

        validarPosseConta(transacao.getContaBancaria());
        return transacao;
    }

    // --- GESTÃO (ADMIN) ---

    public List<TransacaoModel> listarTudoAdmin() {
        if (!isUsuarioAdmin())
            throw new AccessDeniedException("Acesso negado.");
        return transacaoRepository.findAll();
    }

    @Transactional
    public void deletarTransacao(Long id) {
        if (!isUsuarioAdmin())
            throw new AccessDeniedException("Somente administradores deletam registros.");
        if (!transacaoRepository.existsById(id))
            throw new TransacaoNotFoundException("ID inválido.");
        transacaoRepository.deleteById(id);
    }

    // 🔥 Alterado de listarTudoAdmin para listarTodas
    public List<TransacaoModel> listarTodas() {
        if (!isUsuarioAdmin())
            throw new AccessDeniedException("Acesso negado.");
        return transacaoRepository.findAll();
    }

    /**
     * Busca o histórico de uma conta específica validando se o
     * usuário logado tem permissão para vê-la.
     */
    public List<TransacaoModel> buscarPorConta(Long idConta) {
        // 1. Busca a conta no banco
        ContaBancariaModel conta = contaBancariaRepository.findById(idConta)
                .orElseThrow(
                        () -> new ContaBancariaNotFoundException("Conta bancária " + idConta + " não encontrada."));

        // 2. Valida a posse (Segurança baseada no CPF do Token)
        validarPosseConta(conta);

        // 3. Retorna a lista ordenada por data
        return transacaoRepository.findByContaBancariaIdOrderByDataHoraDesc(idConta);
    }
}