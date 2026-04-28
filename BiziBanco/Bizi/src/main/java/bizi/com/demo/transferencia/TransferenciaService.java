package bizi.com.demo.transferencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.transacao.TipoTransacao;

@Service
public class TransferenciaService {

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    private static final BigDecimal LIMITE_TED_HORARIO = new BigDecimal("5000.00");
    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("10000.00");
    private static final LocalTime HORARIO_INICIO_TED = LocalTime.of(6, 30);
    private static final LocalTime HORARIO_FIM_TED = LocalTime.of(17, 0);

    // --- MÉTODOS DE APOIO ---

    private String getEmailLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isUsuarioAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Trava de segurança: impede que CLIENTE acesse dados de outras contas.
     */
    private void validarAcessoConta(Long idConta) {
        if (!isUsuarioAdmin()) {
            // Mudamos de findByUsuarioEmail para findByUsuarioCpf
            ContaBancariaModel contaLogada = contaBancariaRepository.findByUsuarioCpf(getCpfLogado())
                    .orElseThrow(
                            () -> new AccessDeniedException("Usuário não possui conta vinculada ao CPF informado."));

            if (!contaLogada.getId().equals(idConta)) {
                throw new AccessDeniedException("Você não tem permissão para acessar dados de outra conta.");
            }
        }
    }

    // --- LÓGICA PRINCIPAL ---

    private TransacaoModel criarTransacao(ContaBancariaModel conta, TipoTransacao tipo, BigDecimal valor) {
        // Ele chama o novo método passando null ou o CPF da própria conta por padrão
        return criarTransacao(conta, tipo, valor, conta.getUsuario().getCpf(), null);
    }

    private TransacaoModel criarTransacao(ContaBancariaModel conta, TipoTransacao tipo, BigDecimal valor,
            String cpfOrigem, String cpfDestino) {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setTipoTransacao(tipo);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());

        // Aqui os campos que farão o Dashboard funcionar
        transacao.setCpfOrigem(cpfOrigem);
        transacao.setCpfDestino(cpfDestino);

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public TransferenciaDto realizarTransferencia(TransferenciaDto dto) {
        ContaBancariaModel contaOrigem = buscarContaOrigem(dto.getContaOrigem());

        ContaBancariaModel contaDestino = contaBancariaRepository
                .findByNumeroAgenciaAndNumeroConta(dto.getAgenciaDestino(), dto.getNumeroContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de destino inexistente."));

        validarTransferencia(dto, contaOrigem, contaDestino);
        validarContasAtivas(contaOrigem, contaDestino);
        validarSaldo(contaOrigem, dto.getValor());
        validarLimitesDiarios(contaOrigem.getId(), dto.getValor());
        validarHorarioTED(dto.getValor());

        // MOVIMENTAÇÃO
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaBancariaRepository.save(contaOrigem);
        TransacaoModel transacaoSaida = criarTransacao(contaOrigem, TipoTransacao.TRANSFERENCIA_ENVIADA,
                dto.getValor());

        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaBancariaRepository.save(contaDestino);
        criarTransacao(contaDestino, TipoTransacao.TRANSFERENCIA_RECEBIDA, dto.getValor());

        // HISTÓRICO
        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacaoSaida);
        transferencia.setContaDestino(contaDestino.getId());
        transferencia.setAgenciaDestino(dto.getAgenciaDestino());
        transferencia = transferenciaRepository.save(transferencia);

        return montarRecibo(dto, transferencia, contaOrigem, contaDestino, transacaoSaida.getDataHora());
    }

    // --- BUSCAS E FILTROS ---

    public List<TransferenciaModel> buscarPorContaOrigem(Long idConta) {
        validarAcessoConta(idConta);
        return transferenciaRepository.findByTransacaoContaBancariaId(idConta);
    }

    public List<TransferenciaModel> buscarPorContaDestino(Long idConta) {
        validarAcessoConta(idConta);
        return transferenciaRepository.findByContaDestino(idConta);
    }

    public List<TransferenciaModel> buscarTodasDaConta(Long idConta) {
        validarAcessoConta(idConta);
        return transferenciaRepository.findByContaOrigemOrDestino(idConta);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsvExtrato(Long idConta) {
        validarAcessoConta(idConta);

        List<TransferenciaModel> transacoes = transferenciaRepository.findByContaOrigemOrDestino(idConta);

        StringBuilder csv = new StringBuilder();
        csv.append("ID;Data;Valor;Tipo;Destino;Status\n");

        for (TransferenciaModel t : transacoes) {
            boolean isEnvio = t.getTransacao().getContaBancaria().getId().equals(idConta);

            csv.append(t.getId()).append(";")
                    .append(t.getTransacao().getDataHora()).append(";")
                    .append(isEnvio ? t.getTransacao().getValor().negate() : t.getTransacao().getValor()).append(";")
                    .append(isEnvio ? "TRANSFERENCIA ENVIADA" : "TRANSFERENCIA RECEBIDA").append(";")
                    .append(t.getContaDestino()).append(";")
                    .append("CONCLUIDA\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // --- AUXILIARES ---

    private TransferenciaDto montarRecibo(TransferenciaDto dto, TransferenciaModel model, ContaBancariaModel origem,
            ContaBancariaModel destino, LocalDateTime data) {
        TransferenciaDto recibo = new TransferenciaDto();
        recibo.setIdTransferencia(model.getId());
        recibo.setContaOrigem(origem.getId());
        recibo.setAgenciaDestino(dto.getAgenciaDestino());
        recibo.setNumeroContaDestino(dto.getNumeroContaDestino());
        recibo.setValor(dto.getValor());
        recibo.setNomeOrigem(origem.getUsuario().getNomeCompleto());
        recibo.setNomeDestino(destino.getUsuario().getNomeCompleto());
        recibo.setDataHora(data);
        recibo.setStatus("CONCLUIDA");
        recibo.setMensagem("TED enviado com sucesso.");
        return recibo;
    }

    // --- VALIDAÇÕES ---

    private String getCpfLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validarTransferencia(TransferenciaDto dto, ContaBancariaModel origem, ContaBancariaModel destino) {
        if (origem.getId().equals(destino.getId())) {
            throw new RuntimeException("Não é possível transferir para si mesmo.");
        }
        if (dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor deve ser positivo.");
        }
    }

    private void validarContasAtivas(ContaBancariaModel origem, ContaBancariaModel destino) {
        if (!origem.getStatusConta() || !destino.getStatusConta()) {
            throw new RuntimeException("Uma das contas está inativa.");
        }
    }

    private void validarSaldo(ContaBancariaModel conta, BigDecimal valor) {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }
    }

    private void validarLimitesDiarios(Long idConta, BigDecimal valor) {
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<TransacaoModel> historico = transacaoRepository.findByContaBancariaIdAndDataHoraAfter(idConta, inicio);

        BigDecimal totalHoje = historico.stream()
                .filter(t -> t.getTipoTransacao() == TipoTransacao.TRANSFERENCIA_ENVIADA)
                .map(TransacaoModel::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHoje.add(valor).compareTo(LIMITE_DIARIO) > 0) {
            throw new RuntimeException("Limite diário de R$ 10.000,00 excedido.");
        }
    }

    private void validarHorarioTED(BigDecimal valor) {
        if (valor.compareTo(LIMITE_TED_HORARIO) > 0) {
            LocalTime agora = LocalTime.now();
            if (agora.isBefore(HORARIO_INICIO_TED) || agora.isAfter(HORARIO_FIM_TED)) {
                throw new RuntimeException(
                        "TED acima de R$ 5.000 só é permitido em horário comercial (06:30 às 17:00).");
            }
        }
    }

    public TransferenciaModel buscarPorId(Long id) {
        TransferenciaModel t = transferenciaRepository.findById(id)
                .orElseThrow(() -> new TransferenciaNotFoundException("Transferência não encontrada."));

        validarAcessoConta(t.getTransacao().getContaBancaria().getId());
        return t;
    }

    private ContaBancariaModel buscarContaOrigem(Long idDto) {
        if (!isUsuarioAdmin()) {
            return contaBancariaRepository.findByUsuarioCpf(getCpfLogado())
                    .orElseThrow(() -> new ContaBancariaNotFoundException("Sua conta de origem não foi encontrada."));
        }
        return contaBancariaRepository.findById(idDto)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de origem não encontrada."));
    }
}