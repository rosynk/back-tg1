package bizi.com.demo.pix;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.contaBancaria.ContaBancariaRepository;
import bizi.com.demo.contaBancaria.ContaBancariaService;
import bizi.com.demo.transacao.TipoTransacao;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.transacao.TransacaoRepository;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PixService {

    @Autowired
    private PixRepository pixRepository;

    @Autowired
    private ContaBancariaService contaService;

    @Autowired
    private ContaBancariaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Transactional
    public PixModel realizarPix(PixDto dto) {
        // 1. Identifica quem envia
        ContaBancariaModel contaOrigem = contaService.buscarMinhasContas().get(0);

        // 2. Valida saldo
        if (contaOrigem.getSaldo().compareTo(dto.getValor()) < 0) {
            throw new RuntimeException("Saldo insuficiente para concluir o Pix.");
        }

        // 3. Localiza destinatário
        UsuarioModel usuarioDestino = usuarioRepository.findByCpf(dto.getChavePixDestino())
                .orElseGet(() -> usuarioRepository.findByEmail(dto.getChavePixDestino())
                .orElseThrow(() -> new RuntimeException("Chave Pix não encontrada.")));

        ContaBancariaModel contaDestino = contaRepository.findByUsuario(usuarioDestino)
                .orElseThrow(() -> new RuntimeException("Destinatário sem conta ativa."));

        // 4. Movimentação Financeira
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

        // 5. Registo de SAÍDA (Remetente)
        TransacaoModel transacaoSaida = new TransacaoModel();
        transacaoSaida.setContaBancaria(contaOrigem);
        transacaoSaida.setValor(dto.getValor());
        transacaoSaida.setTipoTransacao(TipoTransacao.PIX_SAIDA); // Ajustado para o seu Enum
        transacaoSaida.setDataHora(LocalDateTime.now());
        transacaoSaida = transacaoRepository.save(transacaoSaida);

        // 6. Registo de ENTRADA (Destinatário)
        TransacaoModel transacaoEntrada = new TransacaoModel();
        transacaoEntrada.setContaBancaria(contaDestino);
        transacaoEntrada.setValor(dto.getValor());
        transacaoEntrada.setTipoTransacao(TipoTransacao.PIX_ENTRADA); // Ajustado para o seu Enum
        transacaoEntrada.setDataHora(LocalDateTime.now());
        transacaoRepository.save(transacaoEntrada);

        // 7. Registo dos Detalhes do Pix
        PixModel pix = new PixModel();
        pix.setTransacao(transacaoSaida);
        pix.setChavePixDestino(dto.getChavePixDestino());
        pix.setMensagem(dto.getMensagem());

        return pixRepository.save(pix);
    }

    public List<PixModel> listarPixDoUsuarioLogado() {
        ContaBancariaModel conta = contaService.buscarMinhasContas().get(0);
        return pixRepository.findByTransacao_ContaBancaria(conta);
    }
}