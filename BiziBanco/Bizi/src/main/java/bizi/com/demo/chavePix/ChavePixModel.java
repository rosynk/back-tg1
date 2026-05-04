package bizi.com.demo.chavePix;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chave_pix")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChavePixModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_chave_pix")
	private Long id;

	@Column(name = "data_cadastro", nullable = false)
	private LocalDateTime dataCadastro;

	@JsonProperty("chave")
	private String valor;

	@Column(name = "tipo_chave", nullable = false)
	private String tipoChave;

	@ManyToOne
	@JoinColumn(name = "id_numero_conta", nullable = false)
	private ContaBancariaModel conta;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public String getTipoChave() {
		return tipoChave;
	}

	public void setTipoChave(String tipoChave) {
		this.tipoChave = tipoChave;
	}

	/**
	 * @return String return the valor
	 */
	public String getValor() {
		return valor;
	}

	/**
	 * @param valor the valor to set
	 */
	public void setValor(String valor) {
		this.valor = valor;
	}

	public ContaBancariaModel getConta() {
		return conta;
	}

	public void setConta(ContaBancariaModel conta) {
		this.conta = conta;
	}

}
