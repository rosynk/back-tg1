package bizi.com.demo.pagamentoBoleto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaNotFoundException;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;

@Service
public class PagamentoBoletoService {

    @Autowired
    private PagamentoBoletoRepository pagamentoBoletoRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    // Limite diário de pagamentos de boleto (ajuste conforme sua regra de negócio)
    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("10000.00");

    @Transactional
    public PagamentoBoletoDto realizarPagamento(PagamentoBoletoDto dto) {
        // 1. Validações básicas
        validarPagamento(dto);

        // 2. Buscar conta de origem
        ContaBancariaModel contaOrigem = contaBancariaRepository.findById(dto.getContaOrigem())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de origem não encontrada"));

        // 3. Validações de negócio
        validarContaAtiva(contaOrigem);
        validarSaldo(contaOrigem, dto.getValor());
        validarLimiteDiario(contaOrigem.getId(), dto.getValor());

        // 4. Criar transação de débito
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(contaOrigem);
        transacao.setTipoTransacao("PAGAMENTO_BOLETO");
        transacao.setValor(dto.getValor());
        transacao.setDataHora(LocalDateTime.now());
        transacao = transacaoRepository.save(transacao);

        // 5. Debitar da conta origem
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaBancariaRepository.save(contaOrigem);

        // 6. Registrar pagamento
        PagamentoBoletoModel pagamento = new PagamentoBoletoModel();
        pagamento.setTransacao(transacao);
        pagamento.setCodigoBarras(dto.getCodigoBarras());
        pagamento.setAgBeneficiario(dto.getAgBeneficiario());
        pagamento.setContaBeneficiario(dto.getContaBeneficiario());
        pagamento = pagamentoBoletoRepository.save(pagamento);

        // 7. Montar resposta
        dto.setIdPagamento(pagamento.getId());
        dto.setIdTransacao(transacao.getId());
        dto.setDataHora(transacao.getDataHora());
        dto.setStatus("CONCLUIDO");
        dto.setMensagem("Pagamento realizado com sucesso");
        return dto;
    }

    private void validarPagamento(PagamentoBoletoDto dto) {
        if (dto.getCodigoBarras() == null || dto.getCodigoBarras().isBlank()) {
            throw new PagamentoBoletoException("Código de barras é obrigatório");
        }
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagamentoBoletoException("Valor deve ser maior que zero");
        }
        if (dto.getAgBeneficiario() == null || dto.getAgBeneficiario().isBlank()) {
            throw new PagamentoBoletoException("Agência do beneficiário é obrigatória");
        }
        if (dto.getContaBeneficiario() == null || dto.getContaBeneficiario().isBlank()) {
            throw new PagamentoBoletoException("Conta do beneficiário é obrigatória");
        }
    }

    private void validarContaAtiva(ContaBancariaModel conta) {
        if (!conta.getStatusConta()) {
            throw new PagamentoBoletoException("Conta de origem está inativa");
        }
    }

    private void validarSaldo(ContaBancariaModel conta, BigDecimal valor) {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new PagamentoBoletoException("Saldo insuficiente para realizar o pagamento");
        }
    }

    private void validarLimiteDiario(Long idConta, BigDecimal valorPagamento) {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimHoje = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        BigDecimal totalHoje = pagamentoBoletoRepository.findByContaOrigem(idConta)
                .stream()
                .filter(p -> {
                    LocalDateTime data = p.getTransacao().getDataHora();
                    return data.isAfter(inicioHoje) && data.isBefore(fimHoje);
                })
                .map(p -> p.getTransacao().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHoje.add(valorPagamento).compareTo(LIMITE_DIARIO) > 0) {
            throw new PagamentoBoletoException(
                    String.format("Limite diário de pagamentos excedido. Limite: R$ %.2f", LIMITE_DIARIO));
        }
    }

    public List<PagamentoBoletoModel> buscarPorConta(Long idConta) {
        return pagamentoBoletoRepository.findByContaOrigem(idConta);
    }

    public PagamentoBoletoModel buscarPorId(Long id) {
        return pagamentoBoletoRepository.findById(id)
                .orElseThrow(() -> new PagamentoBoletoNotFoundException("Pagamento não encontrado"));
    }
}