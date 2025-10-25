package bizi.com.demo.pagamentoBoleto;

import bizi.com.demo.transacao.TransacaoModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagamento_boleto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoBoletoModel {

	@Id
    @Column(name = "id_transacao")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_transacao")
    private TransacaoModel transacao;

    @Column(name = "codigo_barras", nullable = false)
    private String codigoBarras;

    @Column(name = "nome_beneficiario")
    private String nomeBeneficiario;
    
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

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public String getNomeBeneficiario() {
		return nomeBeneficiario;
	}

	public void setNomeBeneficiario(String nomeBeneficiario) {
		this.nomeBeneficiario = nomeBeneficiario;
	}

}
