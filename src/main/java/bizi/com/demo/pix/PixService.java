package bizi.com.demo.pix;

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
public class PixService {

    @Autowired
    private PixRepository pixRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    // Regra BACEN: limite noturno Pix (20h–6h) = R$ 1.000
    private static final BigDecimal LIMITE_PIX_NOTURNO = new BigDecimal("1000.00");
    private static final BigDecimal LIMITE_DIARIO_PIX   = new BigDecimal("10000.00");
    private static final LocalTime  INICIO_PERIODO_NOTURNO = LocalTime.of(20, 0);
    private static final LocalTime  FIM_PERIODO_NOTURNO    = LocalTime.of(6, 0);

    @Transactional
    public PixDto realizarPix(PixDto dto) {

        // 1. Validações básicas
        validarPix(dto);

        // 2. Buscar contas
        ContaBancariaModel contaOrigem = contaBancariaRepository.findById(dto.getContaOrigem())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de origem não encontrada"));

        ContaBancariaModel contaDestino = contaBancariaRepository.findById(dto.getContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de destino não encontrada"));

        // 3. Validações de negócio
        validarContasAtivas(contaOrigem, contaDestino);
        validarSaldo(contaOrigem, dto.getValor());
        validarLimiteDiario(contaOrigem.getId(), dto.getValor());
        validarLimiteNoturno(dto.getValor());

        // 4. Transação de débito na origem
        TransacaoModel transacaoDebito = new TransacaoModel();
        transacaoDebito.setContaBancaria(contaOrigem);
        transacaoDebito.setTipoTransacao("PIX");
        transacaoDebito.setValor(dto.getValor());
        transacaoDebito.setDataHora(LocalDateTime.now());
        transacaoDebito = transacaoRepository.save(transacaoDebito);

        // 5. Debitar origem
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaBancariaRepository.save(contaOrigem);

        // 6. Creditar destino
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaBancariaRepository.save(contaDestino);

        // 7. Transação de crédito no destino
        TransacaoModel transacaoCredito = new TransacaoModel();
        transacaoCredito.setContaBancaria(contaDestino);
        transacaoCredito.setTipoTransacao("PIX_RECEBIDO");
        transacaoCredito.setValor(dto.getValor());
        transacaoCredito.setDataHora(LocalDateTime.now());
        transacaoRepository.save(transacaoCredito);

        // 8. Registrar Pix
        PixModel pix = new PixModel();
        pix.setTransacao(transacaoDebito);
        pix.setContaDestino(dto.getContaDestino());
        pix.setChavePix(dto.getChavePix());
        pix = pixRepository.save(pix);

        // 9. Montar resposta
        dto.setIdPix(pix.getId());
        dto.setIdTransacao(transacaoDebito.getId());
        dto.setStatus("CONCLUIDO");
        dto.setDataHora(transacaoDebito.getDataHora());
        return dto;
    }

    // -------------------------------------------------------------------------
    // Validações
    // -------------------------------------------------------------------------

    private void validarPix(PixDto dto) {
        if (dto.getContaOrigem().equals(dto.getContaDestino())) {
            throw new PixException("Não é possível realizar Pix para a mesma conta");
        }
        if (dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PixException("Valor deve ser maior que zero");
        }
        if (dto.getChavePix() == null || dto.getChavePix().isBlank()) {
            throw new PixException("Chave Pix é obrigatória");
        }
    }

    private void validarContasAtivas(ContaBancariaModel origem, ContaBancariaModel destino) {
        if (!origem.getStatusConta()) throw new PixException("Conta de origem está inativa");
        if (!destino.getStatusConta()) throw new PixException("Conta de destino está inativa");
    }

    private void validarSaldo(ContaBancariaModel conta, BigDecimal valor) {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new PixException("Saldo insuficiente para realizar o Pix");
        }
    }

    private void validarLimiteDiario(Long idConta, BigDecimal valorPix) {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimHoje    = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        BigDecimal totalHoje = pixRepository.findByContaOrigem(idConta).stream()
                .filter(p -> {
                    LocalDateTime data = p.getTransacao().getDataHora();
                    return data.isAfter(inicioHoje) && data.isBefore(fimHoje);
                })
                .map(p -> p.getTransacao().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHoje.add(valorPix).compareTo(LIMITE_DIARIO_PIX) > 0) {
            throw new PixException(
                String.format("Limite diário de Pix excedido. Limite: R$ %.2f", LIMITE_DIARIO_PIX));
        }
    }

    /** BACEN: entre 20h e 6h o limite por transação é R$ 1.000 (padrão; usuário pode ampliar até R$ 12k) */
    private void validarLimiteNoturno(BigDecimal valor) {
        LocalTime agora = LocalTime.now();
        boolean periodoNoturno = agora.isAfter(INICIO_PERIODO_NOTURNO) || agora.isBefore(FIM_PERIODO_NOTURNO);

        if (periodoNoturno && valor.compareTo(LIMITE_PIX_NOTURNO) > 0) {
            throw new PixException(
                "Entre 20h e 6h o limite por transação Pix é R$ 1.000,00 (horário noturno BACEN)");
        }
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    public PixModel buscarPorId(Long id) {
        return pixRepository.findById(id)
                .orElseThrow(() -> new PixNotFoundException("Pix não encontrado"));
    }

    public List<PixModel> buscarEnviados(Long idConta) {
        return pixRepository.findByContaOrigem(idConta);
    }

    public List<PixModel> buscarRecebidos(Long idConta) {
        return pixRepository.findByContaDestino(idConta);
    }

    public List<PixModel> buscarTodosDaConta(Long idConta) {
        return pixRepository.findByContaOrigemOrDestino(idConta);
    }
}