package bizi.com.demo.saque; // Ajuste para o seu pacote de preferência

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import bizi.com.demo.contaBancaria.ContaInexistenteException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SaqueService {

    @Autowired
    private ContaBancariaRepository contaRepository;

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Transactional
    public TransacaoModel realizarSaque(BigDecimal valor) {
        // 1. Pega a conta do usuário logado através do token
        ContaBancariaModel conta = contaService.buscarMinhasContas().get(0);

        // 2. Validações de Segurança
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor do saque deve ser positivo.");
        }
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente para realizar o saque.");
        }

        // 3. Atualiza o saldo (Logística de Débito)
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);

        // 4. Registra a transação (para aparecer no seu Extrato)
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setTipoTransacao(TipoTransacao.SAQUE);
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        
        return transacaoRepository.save(transacao);
    }
    
 // Método exclusivo para o ADM (Ex: Encerramento de conta ou correção)
    @Transactional
    public TransacaoModel realizarSaqueAdministrativo(Long idConta, BigDecimal valor) {
        // 1. Busca a conta pelo ID usando o novo nome da Exception
        ContaBancariaModel conta = contaRepository.findById(idConta)
                .orElseThrow(() -> new ContaInexistenteException("Erro na Logística: A conta " + idConta + " não existe no Bizi."));

        // 2. Validação de saldo (Regra de Negócio)
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Operação Administrativa Negada: Saldo insuficiente.");
        }

        // 3. Execução do Débito
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);

        // 4. Registro para o Extrato
        TransacaoModel saque = new TransacaoModel();
        saque.setContaBancaria(conta);
        saque.setTipoTransacao(TipoTransacao.SAQUE);
        saque.setValor(valor);
        saque.setDataHora(LocalDateTime.now());

        return transacaoRepository.save(saque);
    }
}