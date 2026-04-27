package bizi.com.demo.extrato;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException; // Certifique-se de ter essa Exception
import bizi.com.demo.contaBancaria.ContaBancariaRepository; // IMPORTANTE
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.pix.PixRepository;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoService;
import bizi.com.demo.transferencia.TransferenciaRepository;
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
    private PixRepository pixRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired // ADICIONADO: Resolvendo o erro de compilação
    private ContaBancariaRepository contaBancariaRepository;

    // MÉTODO PARA O CLIENTE (Logado)
    public ExtratoResponseDto gerarExtrato() {
        List<ContaBancariaModel> contas = contaService.buscarMinhasContas();
        if (contas.isEmpty()) {
            throw new RuntimeException("Nenhuma conta encontrada para o usuário logado.");
        }
        ContaBancariaModel conta = contas.get(0);
        
        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(conta.getId());
        List<ExtratoDto> transacoesFormatadas = formatarTransacoes(transacoes);

        return new ExtratoResponseDto(
            conta.getUsuario().getNomeCompleto(), 
            conta.getSaldo(), 
            transacoesFormatadas
        );
    }

    // MÉTODO PARA O ADM (Por ID)
    public ExtratoResponseDto gerarExtratoParaAdm(Long idConta) {
        ContaBancariaModel conta = contaBancariaRepository.findById(idConta)
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta não encontrada"));
        
        List<TransacaoModel> transacoes = transacaoService.buscarPorConta(conta.getId());
        List<ExtratoDto> transacoesFormatadas = formatarTransacoes(transacoes);
        
        return new ExtratoResponseDto(
            conta.getUsuario().getNomeCompleto(), 
            conta.getSaldo(), 
            transacoesFormatadas
        );
    }

    // MÉTODO PRIVADO: O "Motor" que formata a lista (Evita repetição de código)
    private List<ExtratoDto> formatarTransacoes(List<TransacaoModel> transacoes) {
        List<ExtratoDto> listaFormatada = new ArrayList<>();

        for (TransacaoModel t : transacoes) {
            String detalhe = "Movimentação";
            BigDecimal valorFormatado = t.getValor();

            // Lógica de sinal negativo para saídas
            if (t.getTipoTransacao() == TipoTransacao.PIX_SAIDA || 
                t.getTipoTransacao() == TipoTransacao.TRANSFERENCIA_ENVIADA ||
                t.getTipoTransacao() == TipoTransacao.SAQUE) {
                valorFormatado = valorFormatado.negate();
            }

            // Busca detalhes de Pix
            if (t.getTipoTransacao() == TipoTransacao.PIX_SAIDA) {
                detalhe = pixRepository.findAll().stream()
                        .filter(p -> p.getTransacao().getId().equals(t.getId()))
                        .map(p -> "Pix para: " + p.getChavePixDestino())
                        .findFirst().orElse("Pagamento Pix");
            } 
            // Busca detalhes de TED
            else if (t.getTipoTransacao() == TipoTransacao.TRANSFERENCIA_ENVIADA) {
                detalhe = "TED para conta: " + t.getContaBancaria().getNumeroConta();
            }

            listaFormatada.add(new ExtratoDto(t.getDataHora(), t.getTipoTransacao().name(), valorFormatado, detalhe));
        }

        // Ordena por data (mais recente primeiro)
        return listaFormatada.stream()
                .sorted(Comparator.comparing(ExtratoDto::getData).reversed())
                .collect(Collectors.toList());
    }
}