package bizi.com.demo.pix;

import bizi.com.demo.transacao.TransacaoModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pix")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pix")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_transacao", nullable = false)
    private TransacaoModel transacao;

    @Column(name = "conta_destino", nullable = false)
    private Long contaDestino;

    // Chave Pix usada na transação (CPF, e-mail, telefone ou aleatória)
    @Column(name = "chave_pix", nullable = false)
    private String chavePix;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransacaoModel getTransacao() { return transacao; }
    public void setTransacao(TransacaoModel transacao) { this.transacao = transacao; }

    public Long getContaDestino() { return contaDestino; }
    public void setContaDestino(Long contaDestino) { this.contaDestino = contaDestino; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
}