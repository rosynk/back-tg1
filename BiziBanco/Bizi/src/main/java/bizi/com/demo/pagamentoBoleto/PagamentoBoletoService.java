package bizi.com.demo.pagamentoBoleto;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PagamentoBoletoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaBancariaRepository contaRepository;

    @Autowired
    private PagamentoBoletoRepository pagamentoRepository;

    @Transactional
    public PagamentoBoletoModel pagarBoleto(String codigoBarras, BigDecimal valor, String beneficiario) {
        // 1. Identifica a conta logada
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ContaBancariaModel conta = contaRepository.findAll().stream()
                .filter(c -> c.getUsuario().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Conta não encontrada."));

        // 2. Valida saldo
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }

        // 3. Atualiza Saldo
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);

        // 4. Cria a Transação
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setValor(valor);
        transacao.setTipoTransacao(TipoTransacao.PAGAMENTO_BOLETO);
        transacao.setDataHora(LocalDateTime.now());
        transacao = transacaoRepository.save(transacao);

        // 5. Salva os detalhes do Boleto (Usando sua Model com MapsId)
        PagamentoBoletoModel pagamento = new PagamentoBoletoModel();
        pagamento.setTransacao(transacao); // O MapsId vai copiar o ID da transação para cá
        pagamento.setCodigoBarras(codigoBarras);
        pagamento.setNomeBeneficiario(beneficiario);

        return pagamentoRepository.save(pagamento);
    }
}