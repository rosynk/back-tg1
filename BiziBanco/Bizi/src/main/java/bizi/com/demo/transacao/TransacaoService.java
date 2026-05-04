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
import bizi.com.demo.usuario.UsuarioRepository;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    public TransacaoModel criarTransacao(ContaBancariaModel conta, TipoTransacao tipo, BigDecimal valor,
            String cpfOrigem, String cpfDestino,
            String nomeContraparte, String detalhe) {

        // 1. Instancia o modelo
        TransacaoModel transacao = new TransacaoModel();

        // 2. Preenche os campos (isso é o que estava faltando!)
        transacao.setContaBancaria(conta);
        transacao.setTipoTransacao(tipo);
        transacao.setValor(valor);
        transacao.setCpfOrigem(cpfOrigem);
        transacao.setCpfDestino(cpfDestino);
        transacao.setNomeContraparte(nomeContraparte); // <--- O nome que você buscou pelo CPF
        transacao.setDetalhe(detalhe); // <--- Ex: "PIX PARA JOÃO"
        transacao.setDataHora(LocalDateTime.now());

        // 3. Salva efetivamente no banco
        return transacaoRepository.save(transacao);
    }
    // --- OPERAÇÕES FINANCEIRAS ---

    public TransacaoModel criarTransacao(TransacaoDto dto) {
        // 1. Busca a conta de origem
        ContaBancariaModel conta = contaBancariaRepository.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não encontrada"));

        String cpfOrigem = conta.getUsuario().getCpf();
        String cpfDestino = dto.getCpfDestino();

        // 2. Busca o nome do favorecido (Mantendo sua lógica de busca por CPF)
        String nomeContraparte;
        var usuarioOpt = usuarioRepository.findByCpf(cpfDestino);

        if (usuarioOpt.isPresent()) {
            nomeContraparte = usuarioOpt.get().getNomeCompleto();
        } else {
            nomeContraparte = "Conta não localizada";
        }

        // --- 🚀 A MÁGICA DA FORMATAÇÃO AQUI ---

        // Transforma "PIX_SAIDA" ou "TED" em algo amigável como "Pix" ou "Ted"
        String tipoAmigavel = dto.getTipoTransacao().toString()
                .split("_")[0] // Pega só a primeira parte (ex: de PIX_SAIDA vira PIX)
                .toLowerCase(); // vira "pix"

        // Deixa a primeira letra maiúscula (vire "Pix")
        tipoAmigavel = tipoAmigavel.substring(0, 1).toUpperCase() + tipoAmigavel.substring(1);

        // Monta a frase: "Pix enviado para João Silva" ou "Ted enviada para Maria"
        // Usamos o nome vindo do banco (nomeContraparte) que geralmente já está correto
        String detalheParaOExtrato = tipoAmigavel + " enviada para " + nomeContraparte;

        // ---------------------------------------

        // 4. Chama o método de persistência com os detalhes formatados
        return criarTransacao(
                conta,
                dto.getTipoTransacao(),
                dto.getValor(),
                cpfOrigem,
                cpfDestino,
                nomeContraparte,
                detalheParaOExtrato);
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
        t.setCpfOrigem(getUsuarioLogado().getCpf());

        // RESOLUÇÃO DO NULL:
        // Se for transferência, o TransferenciaService enviará o nome.
        // Para Saque/Depósito, definimos aqui o que o usuário lerá no extrato.
        if (tipo == TipoTransacao.SAQUE) {
            t.setNomeContraparte("Retirada de Recurso");
        } else if (tipo == TipoTransacao.DEPOSITO) {
            t.setNomeContraparte("Entrada de Recurso");
        }

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