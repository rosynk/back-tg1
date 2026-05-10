package bizi.com.demo.pix;

import bizi.com.demo.chavePix.ChavePixRepository;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.transacao.TransacaoService;
import bizi.com.demo.usuario.UsuarioModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import bizi.com.demo.security.SecurityUtil;

@Service
public class PixService {

    @Autowired
    private PixRepository pixRepository;

    @Autowired
    private ChavePixRepository chavePixRepository; // Repositório do pacote de chaves

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private ContaBancariaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private TransacaoService transacaoService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    

    public Map<String, Object> buscarInformacoesResumo() {
        // 1. Obtém o usuário logado dinamicamente via SecurityUtil
        // Se você não tiver o securityUtil injetado aqui, adicione o @Autowired dele
        UsuarioModel usuarioLogado = securityUtil.getUsuarioLogado();

        // 2. Busca a conta usando o ID do usuário (mais seguro e rápido que CPF)
        // Usamos findFirst() para evitar erros caso o usuário tenha mais de uma conta
        ContaBancariaModel conta = contaRepository.findByUsuarioId(usuarioLogado.getId())
                .stream()
                .findFirst()
                .orElse(null);

        Map<String, Object> resumo = new HashMap<>();

        if (conta == null) {
            // Em vez de erro 500, retornamos um mapa com valores zerados
            // Isso permite que o Angular carregue a tela sem quebrar
            resumo.put("saldo", java.math.BigDecimal.ZERO);
            resumo.put("numeroConta", "Pendente");
            resumo.put("agencia", "0000");
            resumo.put("extrato", java.util.Collections.emptyList());
            return resumo;
        }

        resumo.put("saldo", conta.getSaldo());
        resumo.put("numeroConta", conta.getNumeroConta());
        resumo.put("agencia", conta.getNumeroAgencia());
        resumo.put("extrato", conta.getTransacoes());

        return resumo;
    }

    public Object realizarPix(Object dadosTransferencia) {
        // Implemente aqui a lógica de débito na conta de origem e crédito na destino
        return null;
    }

    @Transactional
    public PixModel realizarPix(PixDto dto) {
        // 1. Identifica a conta de quem envia (contaOrigem)
        ContaBancariaModel contaOrigem = contaService.buscarMinhasContas().get(0);

        // 2. Valida se há saldo suficiente
        if (contaOrigem.getSaldo().compareTo(dto.getValor()) < 0) {
            throw new RuntimeException("Saldo insuficiente para concluir o Pix.");
        }

        // 3. Localiza a conta do destinatário (contaDestino)
        var chaveEncontrada = chavePixRepository.findByValor(dto.getChavePixDestino())
                .orElseThrow(() -> new RuntimeException("Chave Pix não encontrada no sistema."));

        ContaBancariaModel contaDestino = chaveEncontrada.getConta(); // <--- Variável declarada aqui

        // Evita enviar para si mesmo
        if (contaOrigem.getId().equals(contaDestino.getId())) {
            throw new RuntimeException("Não é possível realizar um Pix para a própria conta.");
        }

        // 4. Movimentação Financeira
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

        // 5. Registro de SAÍDA (Usa contaOrigem e o nome de quem recebe)
        String nomeDestinatario = contaDestino.getUsuario().getNomeCompleto();
        TransacaoModel transacaoSaida = transacaoService.criarTransacao(
                contaOrigem,
                TipoTransacao.PIX_SAIDA,
                dto.getValor(),
                contaOrigem.getUsuario().getCpf(),
                contaDestino.getUsuario().getCpf(),
                nomeDestinatario,
                "PIX ENVIADO PARA " + nomeDestinatario);

        // 6. Registro de ENTRADA (Usa contaDestino e o nome de quem envia)
        String nomeRemetente = contaOrigem.getUsuario().getNomeCompleto();
        transacaoService.criarTransacao(
                contaDestino,
                TipoTransacao.PIX_ENTRADA,
                dto.getValor(),
                contaOrigem.getUsuario().getCpf(),
                contaDestino.getUsuario().getCpf(),
                nomeRemetente,
                "PIX RECEBIDO DE " + nomeRemetente);

        // 7. Registro do Pix
        PixModel pix = new PixModel();
        pix.setTransacao(transacaoSaida);
        pix.setChavePixDestino(dto.getChavePixDestino());
        pix.setMensagem(dto.getMensagem());

        return pixRepository.save(pix);
    }

    /**
     * Método auxiliar para evitar repetição de código no registro de transações
     */
    private TransacaoModel registrarTransacao(ContaBancariaModel conta, java.math.BigDecimal valor,
            TipoTransacao tipo) {
        TransacaoModel t = new TransacaoModel();
        t.setContaBancaria(conta);
        t.setValor(valor);
        t.setTipoTransacao(tipo);
        t.setDataHora(LocalDateTime.now());
        return transacaoRepository.save(t);
    }

    public List<PixModel> listarPixDoUsuarioLogado() {
        ContaBancariaModel conta = contaService.buscarMinhasContas().get(0);
        return pixRepository.findByTransacao_ContaBancaria(conta);
    }
}
