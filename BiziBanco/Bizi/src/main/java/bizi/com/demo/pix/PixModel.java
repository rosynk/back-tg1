package bizi.com.demo.pix;

import bizi.com.demo.transacao.TransacaoModel;
import jakarta.persistence.*;

@Entity
@Table(name = "pix_envios")
public class PixModel {   

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pix")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_transacao", nullable = false)
    private TransacaoModel transacao;

    @Column(name = "chave_pix_destino", nullable = false)
    private String chavePixDestino;

    @Column(name = "mensagem")
    private String mensagem; 

    public PixModel() {}

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransacaoModel getTransacao() {
        return transacao;
    }

    public void setTransacao(TransacaoModel transacao) {
        this.transacao = transacao;
    }

    public String getChavePixDestino() {
        return chavePixDestino;
    }

    public void setChavePixDestino(String chavePixDestino) {
        this.chavePixDestino = chavePixDestino;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}