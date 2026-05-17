package bizi.com.demo.transferencia;

import bizi.com.demo.transacao.TransacaoModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transferencia")
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transferencia")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_transacao", nullable = false)
    private TransacaoModel transacao;

    @Column(name = "conta_destino", nullable = false)
    private Long contaDestino;

    @Column(name = "agencia_destino", nullable = false)
    private String agenciaDestino;

    @Column(name = "contra_parte", nullable = false)
    private String nomeContraparte;

    // "TED" ou "DOC"
    @Column(name = "tipo_transferencia", nullable = false, length = 3)
    private String tipoTransferencia;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TransacaoModel getTransacao() { return transacao; }
    public void setTransacao(TransacaoModel transacao) { this.transacao = transacao; }
    public Long getContaDestino() { return contaDestino; }
    public void setContaDestino(Long contaDestino) { this.contaDestino = contaDestino; }
    public String getAgenciaDestino() { return agenciaDestino; }
    public void setAgenciaDestino(String agenciaDestino) { this.agenciaDestino = agenciaDestino; }
    public String getNomeContraparte() { return nomeContraparte; }
    public void setNomeContraparte(String nomeContraparte) { this.nomeContraparte = nomeContraparte; }
    public String getTipoTransferencia() { return tipoTransferencia; }
    public void setTipoTransferencia(String tipoTransferencia) { this.tipoTransferencia = tipoTransferencia; }
}