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

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;

@Service
public class TransferenciaService {

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    private static final BigDecimal LIMITE_DIARIO      = new BigDecimal("10000.00");
    private static final LocalTime  HORARIO_INICIO_TED = LocalTime.of(6, 30);
    private static final LocalTime  HORARIO_FIM_TED    = LocalTime.of(17, 0);

    // -------------------------------------------------------------------------
    // SEGURANÇA E CONTEXTO
    // -------------------------------------------------------------------------

    private String getCpfLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isUsuarioAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validarAcessoConta(Long idConta) {
        if (!isUsuarioAdmin()) {
            ContaBancariaModel contaLogada = contaBancariaRepository
                    .findByUsuarioCpf(getCpfLogado())
                    .orElseThrow(() -> new AccessDeniedException(
                            "Usuário não possui conta vinculada ao CPF informado."));

            if (!contaLogada.getId().equals(idConta))
                throw new AccessDeniedException("Você não tem permissão para acessar dados de outra conta.");
        }
    }

    // -------------------------------------------------------------------------
    // CRIAÇÃO DE TRANSAÇÃO (EXTRATO)
    // -------------------------------------------------------------------------

    private TransacaoModel criarTransacao(ContaBancariaModel conta, TipoTransacao tipo,
            BigDecimal valor, String cpfOrigem, String cpfDestino, String nomeContraparte) {

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setTipoTransacao(tipo);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setCpfOrigem(cpfOrigem);
        transacao.setCpfDestino(cpfDestino);
        transacao.setNomeContraparte(nomeContraparte);
        transacao.setDetalhe(nomeContraparte);
        return transacaoRepository.save(transacao);
    }

    // -------------------------------------------------------------------------
    // OPERAÇÕES PRINCIPAIS
    // -------------------------------------------------------------------------

    @Transactional
    public TransferenciaDto realizarTransferencia(TransferenciaDto dto) {

        ContaBancariaModel contaOrigem = buscarContaOrigem(dto.getContaOrigem());
        ContaBancariaModel contaDestino = contaBancariaRepository
                .findByNumeroAgenciaAndNumeroConta(dto.getAgenciaDestino(), dto.getNumeroContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de destino não encontrada."));

        // Normaliza o tipo — padrão TED se vier nulo
        String tipo = (dto.getTipoTransferencia() != null)
                ? dto.getTipoTransferencia().toUpperCase()
                : "TED";

        // Validações
        validarTransferencia(dto, contaOrigem, contaDestino);
        validarContasAtivas(contaOrigem, contaDestino);
        validarSaldo(contaOrigem, dto.getValor());
        validarLimitesDiarios(contaOrigem.getId(), dto.getValor());
        validarHorarioTED(tipo);

        // Processamento financeiro
        String nomeFavorecido = contaDestino.getUsuario().getNomeCompleto();
        String nomePagador    = contaOrigem.getUsuario().getNomeCompleto();

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaBancariaRepository.save(contaOrigem);
        contaBancariaRepository.save(contaDestino);

        // Descrições do extrato incluem TED/DOC + nome
        String detalheSaida   = tipo + " ENVIADA PARA " + nomeFavorecido.toUpperCase();
        String detalheEntrada = tipo + " RECEBIDA DE "  + nomePagador.toUpperCase();

        TransacaoModel transacaoSaida = criarTransacao(
                contaOrigem,
                TipoTransacao.TRANSFERENCIA_ENVIADA,
                dto.getValor(),
                contaOrigem.getUsuario().getCpf(),
                contaDestino.getUsuario().getCpf(),
                detalheSaida);

        criarTransacao(
                contaDestino,
                TipoTransacao.TRANSFERENCIA_RECEBIDA,
                dto.getValor(),
                contaOrigem.getUsuario().getCpf(),
                contaDestino.getUsuario().getCpf(),
                detalheEntrada);

        // Registro detalhado da transferência
        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacaoSaida);
        transferencia.setAgenciaDestino(dto.getAgenciaDestino());
        transferencia.setContaDestino(contaDestino.getId());
        transferencia.setNomeContraparte(nomeFavorecido);
        transferencia.setTipoTransferencia(tipo);

        transferenciaRepository.save(transferencia);

        return montarRecibo(dto, transferencia, contaOrigem, contaDestino, transacaoSaida.getDataHora());
    }

    @Transactional
    public void estornarTransferencia(Long idTransferencia) {
        if (!isUsuarioAdmin())
            throw new AccessDeniedException("Apenas administradores podem realizar estornos.");

        TransferenciaModel t = transferenciaRepository.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Transferência não encontrada."));

        ContaBancariaModel contaOrigem  = t.getTransacao().getContaBancaria();
        ContaBancariaModel contaDestino = contaBancariaRepository.findById(t.getContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException(
                        "Conta de destino do estorno não encontrada."));

        BigDecimal valor = t.getTransacao().getValor();

        contaDestino.setSaldo(contaDestino.getSaldo().subtract(valor));
        contaOrigem.setSaldo(contaOrigem.getSaldo().add(valor));
        contaBancariaRepository.save(contaOrigem);
        contaBancariaRepository.save(contaDestino);

        criarTransacao(contaOrigem, TipoTransacao.ESTORNO, valor, "SISTEMA", "SISTEMA",
                "Estorno de Transação ID: " + idTransferencia);

        transferenciaRepository.delete(t);
    }

    // -------------------------------------------------------------------------
    // BUSCAS E EXPORTAÇÕES
    // -------------------------------------------------------------------------

    public List<TransferenciaModel> buscarTodasDaConta(Long idConta) {
        validarAcessoConta(idConta);
        return transferenciaRepository.findByContaOrigemOrDestino(idConta);
    }

    public TransferenciaModel buscarPorId(Long id) {
        TransferenciaModel t = transferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transferência não encontrada."));
        validarAcessoConta(t.getTransacao().getContaBancaria().getId());
        return t;
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsvExtrato(Long idConta) {
        validarAcessoConta(idConta);
        List<TransferenciaModel> transacoes = transferenciaRepository.findByContaOrigemOrDestino(idConta);

        StringBuilder csv = new StringBuilder();
        csv.append("ID;Data;Tipo Transferencia;Direcao;Favorecido/Pagador;Valor;Status\n");

        for (TransferenciaModel t : transacoes) {
            boolean isEnvio = t.getTransacao().getContaBancaria().getId().equals(idConta);
            csv.append(t.getId()).append(";")
               .append(t.getTransacao().getDataHora()).append(";")
               .append(t.getTipoTransferencia()).append(";")
               .append(isEnvio ? "ENVIO" : "RECEBIMENTO").append(";")
               .append(t.getNomeContraparte()).append(";")
               .append(isEnvio
                       ? t.getTransacao().getValor().negate()
                       : t.getTransacao().getValor()).append(";")
               .append("CONCLUIDA\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] gerarPdfExtrato(Long idConta) {
        validarAcessoConta(idConta);
        List<TransferenciaModel> transacoes = transferenciaRepository.findByContaOrigemOrDestino(idConta);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Extrato Bancário - BiziBanco", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Conta: " + idConta + " | Gerado em: " + LocalDateTime.now()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("Data");
        table.addCell("Tipo");
        table.addCell("Direção");
        table.addCell("Contraparte");
        table.addCell("Valor");

        for (TransferenciaModel t : transacoes) {
            boolean isEnvio = t.getTransacao().getContaBancaria().getId().equals(idConta);

            table.addCell(t.getTransacao().getDataHora().toString());
            table.addCell(t.getTipoTransferencia());
            table.addCell(isEnvio ? "ENVIO" : "RECEBIMENTO");
            table.addCell(t.getNomeContraparte());

            BigDecimal valor = isEnvio
                    ? t.getTransacao().getValor().negate()
                    : t.getTransacao().getValor();
            PdfPCell cellValor = new PdfPCell(new Phrase("R$ " + valor));
            cellValor.setBackgroundColor(isEnvio ? java.awt.Color.PINK : java.awt.Color.GREEN);
            table.addCell(cellValor);
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    // -------------------------------------------------------------------------
    // AUXILIARES E VALIDAÇÕES
    // -------------------------------------------------------------------------

    private TransferenciaDto montarRecibo(TransferenciaDto dto, TransferenciaModel model,
            ContaBancariaModel origem, ContaBancariaModel destino, LocalDateTime data) {

        TransferenciaDto recibo = new TransferenciaDto();
        recibo.setIdTransferencia(model.getId());
        recibo.setContaOrigem(origem.getId());
        recibo.setAgenciaDestino(dto.getAgenciaDestino());
        recibo.setNumeroContaDestino(dto.getNumeroContaDestino());
        recibo.setValor(dto.getValor());
        recibo.setTipoTransferencia(model.getTipoTransferencia());
        recibo.setNomeOrigem(origem.getUsuario().getNomeCompleto());
        recibo.setNomeDestino(destino.getUsuario().getNomeCompleto());
        recibo.setDataHora(data);
        recibo.setStatus("CONCLUIDA");
        recibo.setMensagem("Transferência " + model.getTipoTransferencia() + " realizada com sucesso.");
        return recibo;
    }

    private void validarTransferencia(TransferenciaDto dto, ContaBancariaModel origem,
            ContaBancariaModel destino) {
        if (origem.getId().equals(destino.getId()))
            throw new RuntimeException("Não é possível transferir para si mesmo.");
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Valor inválido.");
    }

    private void validarContasAtivas(ContaBancariaModel o, ContaBancariaModel d) {
        if (!o.getStatusConta() || !d.getStatusConta())
            throw new RuntimeException("Uma das contas está inativa.");
    }

    private void validarSaldo(ContaBancariaModel conta, BigDecimal valor) {
        if (conta.getSaldo().compareTo(valor) < 0)
            throw new RuntimeException("Saldo insuficiente.");
    }

    private void validarLimitesDiarios(Long idConta, BigDecimal valor) {
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<TransacaoModel> historico =
                transacaoRepository.findByContaBancariaIdAndDataHoraAfter(idConta, inicio);
        BigDecimal totalHoje = historico.stream()
                .filter(t -> t.getTipoTransacao() == TipoTransacao.TRANSFERENCIA_ENVIADA)
                .map(TransacaoModel::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHoje.add(valor).compareTo(LIMITE_DIARIO) > 0)
            throw new RuntimeException("Limite diário de R$ 10.000,00 excedido.");
    }

    private void validarHorarioTED(String tipo) {
        if ("TED".equalsIgnoreCase(tipo)) {
            LocalTime agora = LocalTime.now();
            if (agora.isBefore(HORARIO_INICIO_TED) || agora.isAfter(HORARIO_FIM_TED))
                throw new RuntimeException(
                        "TED só pode ser realizado em horário comercial (06:30 às 17:00).");
        }
        // DOC não tem restrição de horário
    }

    private ContaBancariaModel buscarContaOrigem(Long idDto) {
        if (!isUsuarioAdmin()) {
            return contaBancariaRepository.findByUsuarioCpf(getCpfLogado())
                    .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não localizada."));
        }
        return contaBancariaRepository.findById(idDto)
                .orElseThrow(() -> new ContaBancariaNotFoundException(
                        "Conta id " + idDto + " não encontrada."));
    }
}