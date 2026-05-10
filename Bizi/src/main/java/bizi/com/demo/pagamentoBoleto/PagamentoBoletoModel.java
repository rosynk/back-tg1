package bizi.com.demo.pagamentoBoleto;

import bizi.com.demo.transacao.TransacaoModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagamento_boleto")
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoBoletoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_transacao", nullable = false)
    private TransacaoModel transacao;

    @Column(name = "codigo_barras", nullable = false, length = 48)
    private String codigoBarras;

    @Column(name = "ag_beneficiario", nullable = false)
    private String agBeneficiario;

    @Column(name = "conta_beneficiario", nullable = false)
    private String contaBeneficiario;
    
    @Column(name = "nome_beneficiario", nullable = false)
    private String nomeBeneficiario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransacaoModel getTransacao() { return transacao; }
    public void setTransacao(TransacaoModel transacao) { this.transacao = transacao; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getAgBeneficiario() { return agBeneficiario; }
    public void setAgBeneficiario(String agBeneficiario) { this.agBeneficiario = agBeneficiario; }

    public String getContaBeneficiario() { return contaBeneficiario; }
    public void setContaBeneficiario(String contaBeneficiario) { this.contaBeneficiario = contaBeneficiario; }

    
    public String getNomeBeneficiario() { return nomeBeneficiario; }
    public void setNomeBeneficiario(String nomeBeneficiario) { this.nomeBeneficiario = nomeBeneficiario; }
}