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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagamentoBoletoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaBancariaRepository contaRepository;

    @Autowired
    private PagamentoBoletoRepository pagamentoRepository;
    
    // A LINHA DO ERRO FOI REMOVIDA DAQUI (Injeção circular de Service)

    // 1. Método que o Controller usa para enviar o DTO
    @Transactional
    public PagamentoBoletoModel realizarPagamento(PagamentoBoletoDto dto) {
        return pagarBoleto(dto.getCodigoBarras(), dto.getValor(), dto.getNomeBeneficiario());
    }

    @Transactional
    public PagamentoBoletoModel pagarBoleto(String codigoBarras, BigDecimal valor, String beneficiario) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // No BiziBanco, buscamos a conta vinculada ao e-mail do usuário logado
        ContaBancariaModel conta = contaRepository.findAll().stream()
                .filter(c -> c.getUsuario().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Conta não encontrada."));

        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }

        // Atualiza o saldo
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);

        // Registra a transação genérica
        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);
        transacao.setValor(valor);
        transacao.setTipoTransacao(TipoTransacao.PAGAMENTO_BOLETO);
        transacao.setDataHora(LocalDateTime.now());
        transacao = transacaoRepository.save(transacao);

        // Registra o detalhe do pagamento de boleto
        PagamentoBoletoModel pagamento = new PagamentoBoletoModel();
        pagamento.setTransacao(transacao);
        pagamento.setCodigoBarras(codigoBarras);
        pagamento.setNomeBeneficiario(beneficiario);

        return pagamentoRepository.save(pagamento);
    }

    public PagamentoBoletoDto buscarPorId(Long id) {
        // 1. Busca a Model no banco de dados.
        PagamentoBoletoModel model = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento com ID " + id + " não encontrado no BiziBanco."));

        // 2. Transforma a Model em DTO usando o construtor da classe DTO.
        return new PagamentoBoletoDto(model);
    }
    
    public List<PagamentoBoletoDto> buscarPorConta(Long contaId) {
        return pagamentoRepository.findByTransacao_ContaBancaria_Id(contaId).stream()
                .map(PagamentoBoletoDto::new) // Converte cada Model da lista para DTO
                .collect(Collectors.toList());
    }
}