package bizi.com.demo.transferencia;

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

	public Long getContaDestino() {
		return contaDestino;
	}

	public void setContaDestino(Long contaDestino) {
		this.contaDestino = contaDestino;
	}

	public String getAgenciaDestino() {
		return agenciaDestino;
	}

	public void setAgenciaDestino(String agenciaDestino) {
		this.agenciaDestino = agenciaDestino;
	}

	/**
	 * @return String return the nomeContraparte
	 */
	public String getNomeContraparte() {
		return nomeContraparte;
	}

	/**
	 * @param nomeContraparte the nomeContraparte to set
	 */
	public void setNomeContraparte(String nomeContraparte) {
		this.nomeContraparte = nomeContraparte;
	}

}
