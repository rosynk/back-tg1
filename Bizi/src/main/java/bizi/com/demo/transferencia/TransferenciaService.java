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

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

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

    private static final BigDecimal LIMITE_TED_HORARIO = new BigDecimal("5000.00");
    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("10000.00");
    private static final LocalTime HORARIO_INICIO_TED = LocalTime.of(6, 30);
    private static final LocalTime HORARIO_FIM_TED = LocalTime.of(17, 0);

    // --- MÉTODOS DE SEGURANÇA E CONTEXTO ---

    private String getCpfLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isUsuarioAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validarAcessoConta(Long idConta) {
        if (!isUsuarioAdmin()) {
            ContaBancariaModel contaLogada = contaBancariaRepository.findByUsuarioCpf(getCpfLogado())
                    .orElseThrow(
                            () -> new AccessDeniedException("Usuário não possui conta vinculada ao CPF informado."));

            if (!contaLogada.getId().equals(idConta)) {
                throw new AccessDeniedException("Você não tem permissão para acessar dados de outra conta.");
            }
        }
    }

    // --- LÓGICA DE CRIAÇÃO DE TRANSAÇÃO (EXTRATO) ---

    private TransacaoModel criarTransacao(ContaBancariaModel conta, TipoTransacao tipo, BigDecimal valor,
            String cpfOrigem, String cpfDestino, String nomeContraparte) {
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setTipoTransacao(tipo);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setCpfOrigem(cpfOrigem);
        transacao.setCpfDestino(cpfDestino);
        transacao.setNomeContraparte(nomeContraparte);

        // ADICIONE ESTA LINHA:
        // Garanta que o campo que gera a descrição receba o nome real em vez de null
        transacao.setNomeContraparte(nomeContraparte);
        return transacaoRepository.save(transacao);
    }

    // --- OPERAÇÕES PRINCIPAIS ---

    @Transactional
    public TransferenciaDto realizarTransferencia(TransferenciaDto dto) {
        ContaBancariaModel contaOrigem = buscarContaOrigem(dto.getContaOrigem());
        ContaBancariaModel contaDestino = contaBancariaRepository
                .findByNumeroAgenciaAndNumeroConta(dto.getAgenciaDestino(), dto.getNumeroContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de destino não encontrada."));

        // Validações
        validarTransferencia(dto, contaOrigem, contaDestino);
        validarContasAtivas(contaOrigem, contaDestino);
        validarSaldo(contaOrigem, dto.getValor());
        validarLimitesDiarios(contaOrigem.getId(), dto.getValor());
        validarHorarioTED(dto.getValor());

        // Processamento financeiro
        String nomeFavorecido = contaDestino.getUsuario().getNomeCompleto();
        String nomePagador = contaOrigem.getUsuario().getNomeCompleto();

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));

        contaBancariaRepository.save(contaOrigem);
        contaBancariaRepository.save(contaDestino);

        // 1. Formatação para o extrato de quem ENVIA
        String detalheSaida = "TRANSFERÊNCIA PARA " + nomeFavorecido.toUpperCase();

        TransacaoModel transacaoSaida = criarTransacao(
                contaOrigem,
                TipoTransacao.TRANSFERENCIA_ENVIADA,
                dto.getValor(),
                contaOrigem.getUsuario().getCpf(),
                contaDestino.getUsuario().getCpf(),
                detalheSaida);

        // 2. Formatação para o extrato de quem RECEBE
        // Ajustado para que quem recebe veja: "TRANSFERÊNCIA DE [NOME]"
        String detalheEntrada = "TRANSFERÊNCIA DE " + nomePagador.toUpperCase();

        criarTransacao(
                contaDestino,
                TipoTransacao.TRANSFERENCIA_RECEBIDA,
                dto.getValor(),
                contaOrigem.getUsuario().getCpf(),
                contaDestino.getUsuario().getCpf(),
                detalheEntrada);

        // Registro detalhado da Transferência
        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacaoSaida);
        transferencia.setAgenciaDestino(dto.getAgenciaDestino());
        transferencia.setContaDestino(contaDestino.getId());
        transferencia.setNomeContraparte(nomeFavorecido);

        transferenciaRepository.save(transferencia);

        // Retorna o recibo final
        return montarRecibo(dto, transferencia, contaOrigem, contaDestino, transacaoSaida.getDataHora());
    }

    /**
     * 🔥 NOVO MÉTODO: Estorno de transferência (Exclusivo para ADMIN)
     */
    @Transactional
    public void estornarTransferencia(Long idTransferencia) {
        if (!isUsuarioAdmin())
            throw new AccessDeniedException("Apenas administradores podem realizar estornos.");

        TransferenciaModel t = transferenciaRepository.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Transferência não encontrada."));

        ContaBancariaModel contaOrigem = t.getTransacao().getContaBancaria();
        ContaBancariaModel contaDestino = contaBancariaRepository.findById(t.getContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de destino do estorno não encontrada."));

        BigDecimal valor = t.getTransacao().getValor();

        // Inverte os saldos
        contaDestino.setSaldo(contaDestino.getSaldo().subtract(valor));
        contaOrigem.setSaldo(contaOrigem.getSaldo().add(valor));

        contaBancariaRepository.save(contaOrigem);
        contaBancariaRepository.save(contaDestino);

        // Registra o estorno no extrato
        criarTransacao(contaOrigem, TipoTransacao.ESTORNO, valor, "SISTEMA", "SISTEMA",
                "Estorno de Transação ID: " + idTransferencia);

        // Remove ou marca como estornada (dependendo da sua regra)
        transferenciaRepository.delete(t);
    }

    // --- BUSCAS E FILTROS ---

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
        csv.append("ID;Data;Valor;Tipo;Favorecido/Pagador;Status\n");

        for (TransferenciaModel t : transacoes) {
            boolean isEnvio = t.getTransacao().getContaBancaria().getId().equals(idConta);
            csv.append(t.getId()).append(";")
                    .append(t.getTransacao().getDataHora()).append(";")
                    .append(isEnvio ? t.getTransacao().getValor().negate() : t.getTransacao().getValor()).append(";")
                    .append(isEnvio ? "ENVIO" : "RECEBIMENTO").append(";")
                    .append(t.getNomeContraparte()).append(";")
                    .append("CONCLUIDA\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // --- AUXILIARES E VALIDAÇÕES ---

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
        recibo.setMensagem("Transferência realizada com sucesso.");
        return recibo;
    }

    private void validarTransferencia(TransferenciaDto dto, ContaBancariaModel origem, ContaBancariaModel destino) {
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
        List<TransacaoModel> historico = transacaoRepository.findByContaBancariaIdAndDataHoraAfter(idConta, inicio);
        BigDecimal totalHoje = historico.stream()
                .filter(t -> t.getTipoTransacao() == TipoTransacao.TRANSFERENCIA_ENVIADA)
                .map(TransacaoModel::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHoje.add(valor).compareTo(LIMITE_DIARIO) > 0)
            throw new RuntimeException("Limite diário excedido.");
    }

    private void validarHorarioTED(BigDecimal valor) {
        if (valor.compareTo(LIMITE_TED_HORARIO) > 0) {
            LocalTime agora = LocalTime.now();
            if (agora.isBefore(HORARIO_INICIO_TED) || agora.isAfter(HORARIO_FIM_TED)) {
                throw new RuntimeException("TED acima de R$ 5.000,00 apenas em horário comercial.");
            }
        }
    }

    private ContaBancariaModel buscarContaOrigem(Long idDto) {
        if (!isUsuarioAdmin()) {
            return contaBancariaRepository.findByUsuarioCpf(getCpfLogado())
                    .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não localizada."));
        }
        return contaBancariaRepository.findById(idDto)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta id " + idDto + " não encontrada."));
    }

    public byte[] gerarPdfExtrato(Long idConta) {
        validarAcessoConta(idConta);
        List<TransferenciaModel> transacoes = transferenciaRepository.findByContaOrigemOrDestino(idConta);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Título
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Extrato Bancário - BiziBanco", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Conta: " + idConta + " | Gerado em: " + LocalDateTime.now()));
        document.add(new Paragraph(" ")); // Espaço

        // Tabela
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell("Data");
        table.addCell("Tipo");
        table.addCell("Contraparte");
        table.addCell("Valor");

        for (TransferenciaModel t : transacoes) {
            boolean isEnvio = t.getTransacao().getContaBancaria().getId().equals(idConta);

            table.addCell(t.getTransacao().getDataHora().toString());
            table.addCell(isEnvio ? "ENVIO" : "RECEBIMENTO");
            table.addCell(t.getNomeContraparte());

            BigDecimal valor = isEnvio ? t.getTransacao().getValor().negate() : t.getTransacao().getValor();
            PdfPCell cellValor = new PdfPCell(new Phrase("R$ " + valor.toString()));
            cellValor.setBackgroundColor(isEnvio ? java.awt.Color.PINK : java.awt.Color.GREEN);
            table.addCell(cellValor);
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }
}