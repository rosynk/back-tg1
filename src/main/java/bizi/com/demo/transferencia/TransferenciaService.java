package bizi.com.demo.transferencia;

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
public class TransferenciaService {

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private ContaBancariaRepository contaBancariaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    // Limites do BACEN
    private static final BigDecimal LIMITE_TED_HORARIO = new BigDecimal("5000.00");
    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("10000.00");
    private static final LocalTime HORARIO_INICIO_TED = LocalTime.of(6, 30);
    private static final LocalTime HORARIO_FIM_TED = LocalTime.of(17, 0);

    /**
     * Realiza uma transferência bancária seguindo regras do BACEN
     */
    @Transactional
    public TransferenciaDto realizarTransferencia(TransferenciaDto dto) {
        // 1. Validações iniciais
        validarTransferencia(dto);

        // 2. Buscar contas
        ContaBancariaModel contaOrigem = contaBancariaRepository.findById(dto.getContaOrigem())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de origem não encontrada"));

        ContaBancariaModel contaDestino = contaBancariaRepository.findById(dto.getContaDestino())
                .orElseThrow(() -> new ContaBancariaNotFoundException("Conta de destino não encontrada"));

        // 3. Validações de negócio (BACEN)
        validarContasAtivas(contaOrigem, contaDestino);
        validarSaldo(contaOrigem, dto.getValor());
        validarLimitesDiarios(contaOrigem.getId(), dto.getValor());
        validarHorarioTED(dto.getValor());

        // 4. Criar transação de débito na conta origem
        TransacaoModel transacaoDebito = new TransacaoModel();
        transacaoDebito.setContaBancaria(contaOrigem);
        transacaoDebito.setTipoTransacao("TRANSFERENCIA");
        transacaoDebito.setValor(dto.getValor());
        transacaoDebito.setDataHora(LocalDateTime.now());
        transacaoDebito = transacaoRepository.save(transacaoDebito);

        // 5. Debitar da conta origem
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaBancariaRepository.save(contaOrigem);

        // 6. Creditar na conta destino
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaBancariaRepository.save(contaDestino);

        // 7. Criar transação de crédito na conta destino
        TransacaoModel transacaoCredito = new TransacaoModel();
        transacaoCredito.setContaBancaria(contaDestino);
        transacaoCredito.setTipoTransacao("DEPOSITO");
        transacaoCredito.setValor(dto.getValor());
        transacaoCredito.setDataHora(LocalDateTime.now());
        transacaoRepository.save(transacaoCredito);

        // 8. Registrar transferência
        TransferenciaModel transferencia = new TransferenciaModel();
        transferencia.setTransacao(transacaoDebito);
        transferencia.setContaDestino(dto.getContaDestino());
        transferencia.setAgenciaDestino(dto.getAgenciaDestino());
        transferencia = transferenciaRepository.save(transferencia);

        // 9. Retornar resposta
        TransferenciaModel transferenciaModel = new TransferenciaModel();
        // usuario.setNomeCompleto(usuarioDto.getNomeCompleto());
        transferenciaModel.setAgenciaDestino(dto.getAgenciaDestino());
        transferenciaModel.setContaDestino(dto.getContaDestino());
        transferenciaModel.setId(dto.getIdTransferencia());
        transferenciaModel.setTransacao(transacaoDebito);
		return dto;
        
    }

    /**
     * Valida dados básicos da transferência
     */
    private void validarTransferencia(TransferenciaDto dto) {
        if (dto.getContaOrigem().equals(dto.getContaDestino())) {
            throw new TransferenciaException("Não é possível transferir para a mesma conta");
        }

        if (dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaException("Valor deve ser maior que zero");
        }
    }

    /**
     * Valida se as contas estão ativas
     */
    private void validarContasAtivas(ContaBancariaModel origem, ContaBancariaModel destino) {
        if (!origem.getStatusConta()) {
            throw new TransferenciaException("Conta de origem está inativa");
        }
        if (!destino.getStatusConta()) {
            throw new TransferenciaException("Conta de destino está inativa");
        }
    }

    /**
     * Valida saldo suficiente (BACEN)
     */
    private void validarSaldo(ContaBancariaModel conta, BigDecimal valor) {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new TransferenciaException("Saldo insuficiente para realizar a transferência");
        }
    }

    /**
     * Valida limites diários (simulação de regra BACEN)
     */
    private void validarLimitesDiarios(Long idConta, BigDecimal valorTransferencia) {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimHoje = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        List<TransferenciaModel> transferenciasHoje = transferenciaRepository.findByContaOrigem(idConta)
                .stream()
                .filter(t -> {
                    LocalDateTime dataTransf = t.getTransacao().getDataHora();
                    return dataTransf.isAfter(inicioHoje) && dataTransf.isBefore(fimHoje);
                })
                .toList();

        BigDecimal totalHoje = transferenciasHoje.stream()
                .map(t -> t.getTransacao().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal novoTotal = totalHoje.add(valorTransferencia);

        if (novoTotal.compareTo(LIMITE_DIARIO) > 0) {
            throw new TransferenciaException(
                    String.format("Limite diário de transferências excedido. Limite: R$ %.2f", LIMITE_DIARIO));
        }
    }

    /**
     * Valida horário para TED (BACEN: 6h30 às 17h em dias úteis)
     */
    private void validarHorarioTED(BigDecimal valor) {
        LocalTime agora = LocalTime.now();

        // Se valor > R$ 5.000, valida horário TED
        if (valor.compareTo(LIMITE_TED_HORARIO) > 0) {
            if (agora.isBefore(HORARIO_INICIO_TED) || agora.isAfter(HORARIO_FIM_TED)) {
                throw new TransferenciaException(
                        "Transferências acima de R$ 5.000,00 só podem ser realizadas entre 6h30 e 17h (horário TED)");
            }
        }
    }

    /**
     * Busca transferências por conta (enviadas)
     */
    public List<TransferenciaModel> buscarPorContaOrigem(Long idConta) {
        return transferenciaRepository.findByContaOrigem(idConta);
    }

    /**
     * Busca transferências por conta (recebidas)
     */
    public List<TransferenciaModel> buscarPorContaDestino(Long idConta) {
        return transferenciaRepository.findByContaDestino(idConta);
    }

    /**
     * Busca todas as transferências de uma conta (enviadas e recebidas)
     */
    public List<TransferenciaModel> buscarTodasDaConta(Long idConta) {
        return transferenciaRepository.findByContaOrigemOrDestino(idConta);
    }

    /**
     * Busca transferência por ID
     */
    public TransferenciaModel buscarPorId(Long id) {
        return transferenciaRepository.findById(id)
                .orElseThrow(() -> new TransferenciaNotFoundException("Transferência não encontrada"));
    }
}