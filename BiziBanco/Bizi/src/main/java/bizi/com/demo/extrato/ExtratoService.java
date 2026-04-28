package bizi.com.demo.extrato;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExtratoService {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    public ExtratoResponseDto gerarExtrato() {
        List<ContaBancariaModel> contas = contaService.buscarMinhasContas();

        if (contas == null || contas.isEmpty()) {
            throw new RuntimeException("Usuário não possui conta bancária ativa.");
        }

        // Pega a primeira conta encontrada
        ContaBancariaModel conta = contas.get(0);

        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(conta.getId());
        List<ExtratoDto> transacoesFormatadas = formatarTransacoes(transacoes);

        String nomeTitular = (conta.getUsuario() != null) ? conta.getUsuario().getNomeCompleto()
                : "Titular não identificado";

        return new ExtratoResponseDto(
                nomeTitular,
                conta.getSaldo() != null ? conta.getSaldo() : BigDecimal.ZERO,
                transacoesFormatadas);
    }

    public ExtratoResponseDto gerarExtratoParaAdm(Long idConta) {
        ContaBancariaModel conta = contaBancariaRepository.findById(idConta)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta ID " + idConta + " não encontrada."));

        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(conta.getId());
        List<ExtratoDto> transacoesFormatadas = formatarTransacoes(transacoes);

        return new ExtratoResponseDto(
                conta.getUsuario().getNomeCompleto(),
                conta.getSaldo(),
                transacoesFormatadas);
    }

    private List<ExtratoDto> formatarTransacoes(List<TransacaoModel> transacoes) {
        List<ExtratoDto> listaFormatada = new ArrayList<>();

        if (transacoes == null)
            return listaFormatada;

        for (TransacaoModel t : transacoes) {
            try {
                BigDecimal valor = (t.getValor() != null) ? t.getValor() : BigDecimal.ZERO;
                String tipoNome = (t.getTipoTransacao() != null) ? t.getTipoTransacao().name() : "OUTROS";

                // Lógica de sinal para saídas
                if (t.getTipoTransacao() == TipoTransacao.PIX_SAIDA ||
                        t.getTipoTransacao() == TipoTransacao.TRANSFERENCIA_ENVIADA ||
                        t.getTipoTransacao() == TipoTransacao.SAQUE) {
                    valor = valor.negate();
                }

                // Detalhe simplificado para evitar múltiplas consultas ao banco no loop
                String detalhe = "Transação de " + tipoNome;

                listaFormatada.add(new ExtratoDto(
                        t.getDataHora(),
                        tipoNome,
                        valor,
                        detalhe));
            } catch (Exception e) {
                System.err.println("Pulei uma transação com erro no ID: " + t.getId());
            }
        }

        // Ordenar: Mais recente primeiro
        return listaFormatada.stream()
                .sorted(Comparator.comparing(ExtratoDto::getData, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}