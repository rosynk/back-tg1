package bizi.com.demo.pix;

import bizi.com.demo.transacao.TransacaoModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity // Faltava essa anotação para o Spring reconhecer como tabela
@Table(name = "pix_operacoes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com a transação genérica (extrato)
    @OneToOne
    @JoinColumn(name = "id_transacao", nullable = false)
    private TransacaoModel transacao;

    // Campo que o seu PixService.java está tentando acessar
    @Column(name = "chave_destino", nullable = false)
    private String chavePixDestino;

    private String mensagem;

    // Getters e Setters manuais para garantir que o VS Code encontre
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