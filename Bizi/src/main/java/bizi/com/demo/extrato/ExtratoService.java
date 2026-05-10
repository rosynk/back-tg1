package bizi.com.demo.extrato;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class ExtratoService {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    // --- MÉTODOS DE FORMATAÇÃO ---

    private List<ExtratoDto> formatarTransacoes(List<TransacaoModel> transacoes) {
        List<ExtratoDto> listaFormatada = new ArrayList<>();

        for (TransacaoModel t : transacoes) {
            BigDecimal valor = t.getValor();
            String descricao;

            // Ajuste na detecção: Se o nome do tipo contém "ENVIADA" ou "SAIDA"
            boolean ehSaida = t.getTipoTransacao().name().contains("ENVIADA") ||
                    t.getTipoTransacao().name().endsWith("_SAIDA");

            if (ehSaida) {
                valor = valor.negate(); // Torna o valor negativo para o cálculo
                descricao = "PAGTO PARA "
                        + (t.getNomeContraparte() != null ? t.getNomeContraparte() : t.getCpfDestino());
            } else {
                descricao = "CRED DE " + (t.getNomeContraparte() != null ? t.getNomeContraparte() : t.getCpfOrigem());
            }

            listaFormatada.add(new ExtratoDto(
                    t.getDataHora(),
                    t.getTipoTransacao().name(),
                    valor,
                    descricao,
                    t.getNomeContraparte()));
        }
        return listaFormatada;
    }

    // Atualize também este método para o PDF
    private boolean isSaida(TipoTransacao tipo) {
        // Esta versão garante que "TRANSFERENCIA_ENVIADA" seja detectada como Débito
        return tipo.name().contains("ENVIADA") || tipo.name().endsWith("_SAIDA");
    }

    // --- MÉTODOS PRINCIPAIS ---

    public ExtratoResponseDto gerarExtrato(LocalDate inicio, LocalDate fim) {
        List<ContaBancariaModel> contas = contaService.buscarMinhasContas();
        if (contas == null || contas.isEmpty()) {
            throw new RuntimeException("Usuário não possui conta bancária ativa.");
        }

        ContaBancariaModel conta = contas.get(0);
        LocalDateTime dataHoraInicio = inicio.atStartOfDay();
        LocalDateTime dataHoraFim = fim.atTime(23, 59, 59);

        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(conta.getId())
                .stream()
                .filter(t -> !t.getDataHora().isBefore(dataHoraInicio) && !t.getDataHora().isAfter(dataHoraFim))
                .sorted(Comparator.comparing(TransacaoModel::getDataHora).reversed())
                .collect(Collectors.toList());

        return new ExtratoResponseDto(
                conta.getUsuario().getNomeCompleto(),
                conta.getSaldo(),
                formatarTransacoes(transacoes));
    }

    public byte[] gerarExtratoPdf(LocalDate inicio, LocalDate fim) {
        List<ContaBancariaModel> contas = contaService.buscarMinhasContas();
        if (contas == null || contas.isEmpty()) {
            throw new RuntimeException("Conta não encontrada.");
        }
        ContaBancariaModel conta = contas.get(0);

        LocalDateTime dataHoraInicio = inicio.atStartOfDay();
        LocalDateTime dataHoraFim = fim.atTime(23, 59, 59);

        List<TransacaoModel> transacoesPeriodo = transacaoService.buscarPorConta(conta.getId()).stream()
                .filter(t -> !t.getDataHora().isBefore(dataHoraInicio) && !t.getDataHora().isAfter(dataHoraFim))
                .sorted(Comparator.comparing(TransacaoModel::getDataHora))
                .collect(Collectors.toList());

        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Estilos
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fontVermelha = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.RED);
        Font fontVerde = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(0, 128, 0));

        // Cabeçalho
        Paragraph pTitulo = new Paragraph("BANCO BIZI - EXTRATO DA CONTA", fontTitulo);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(pTitulo);
        document.add(new Paragraph("Titular: " + conta.getUsuario().getNomeCompleto(), fontNormal));
        document.add(new Paragraph("Conta: " + conta.getNumeroConta() + " | Agência: " + conta.getNumeroAgencia(),
                fontNormal));
        document.add(new Paragraph("Período: " + inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " até "
                + fim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
        document.add(new Paragraph(" "));

        // Tabela
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 18, 25, 25, 16, 16 });

        String[] headers = { "DATA/HORA", "DESCRIÇÃO", "NOME", "CRÉDITO", "DÉBITO" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
            cell.setBackgroundColor(new Color(15, 23, 42));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (TransacaoModel t : transacoesPeriodo) {
            boolean isDebito = isSaida(t.getTipoTransacao());

            table.addCell(new Phrase(t.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")), fontNormal));
            table.addCell(new Phrase(t.getTipoTransacao().name().replace("_", " "), fontNormal));
            table.addCell(new Phrase(t.getNomeContraparte() != null ? t.getNomeContraparte() : "-", fontNormal));

            if (isDebito) {
                table.addCell(new Phrase("-", fontNormal));
                PdfPCell cValor = new PdfPCell(new Phrase("R$ " + t.getValor(), fontVermelha));
                cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cValor);
                totalSaidas = totalSaidas.add(t.getValor());
            } else {
                PdfPCell cValor = new PdfPCell(new Phrase("R$ " + t.getValor(), fontVerde));
                cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cValor);
                table.addCell(new Phrase("-", fontNormal));
                totalEntradas = totalEntradas.add(t.getValor());
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("RESUMO DO PERÍODO", fontSub));
        document.add(new Paragraph("(+) Total de Entradas: R$ " + totalEntradas, fontVerde));
        document.add(new Paragraph("(-) Total de Saídas: R$ " + totalSaidas, fontVermelha));
        document.add(new Paragraph("(=) Saldo Atual: R$ " + conta.getSaldo(), fontSub));

        document.close();
        return out.toByteArray();
    }

    public ExtratoResponseDto gerarExtratoParaAdm(Long idConta) {
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusDays(30);
        return gerarExtratoParaAdmComPeriodo(idConta, inicio, fim);
    }

    public ExtratoResponseDto gerarExtratoParaAdmComPeriodo(Long idConta, LocalDate inicio, LocalDate fim) {
        ContaBancariaModel conta = contaBancariaRepository.findById(idConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada com ID: " + idConta));

        LocalDateTime dataHoraInicio = inicio.atStartOfDay();
        LocalDateTime dataHoraFim = fim.atTime(23, 59, 59);

        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(conta.getId())
                .stream()
                .filter(t -> !t.getDataHora().isBefore(dataHoraInicio) && !t.getDataHora().isAfter(dataHoraFim))
                .sorted(Comparator.comparing(TransacaoModel::getDataHora).reversed())
                .collect(Collectors.toList());

        return new ExtratoResponseDto(
                conta.getUsuario().getNomeCompleto(),
                conta.getSaldo(),
                formatarTransacoes(transacoes));
    }
} // Agora sim, fim da classe correto.