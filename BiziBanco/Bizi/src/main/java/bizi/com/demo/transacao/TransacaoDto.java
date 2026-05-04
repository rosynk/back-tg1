package bizi.com.demo.transacao;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class TransacaoDto {

	@NotNull(message = "ID da conta é obrigatório")
	private Long idConta;

	@NotNull(message = "Tipo de transação é obrigatório")
	private TipoTransacao tipoTransacao; // Usando o Enum para evitar Strings erradas

	@NotNull(message = "Valor é obrigatório")
	@DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
	private BigDecimal valor;

	// --- CAMPOS QUE ESTAVAM FALTANDO ---

	private String cpfDestino; // Necessário para transferência/PIX

	private String nomeContraparte; // Opcional: Se o front já souber o nome

	private String detalhe; // Opcional: Para mensagens personalizadas

	// Construtor padrão para o Jackson (JSON)
	public TransacaoDto() {
	}

	// Getters e Setters
	public Long getIdConta() {
		return idConta;
	}

	public void setIdConta(Long idConta) {
		this.idConta = idConta;
	}

	public TipoTransacao getTipoTransacao() {
		return tipoTransacao;
	}

	public void setTipoTransacao(TipoTransacao tipoTransacao) {
		this.tipoTransacao = tipoTransacao;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public String getCpfDestino() {
		return cpfDestino;
	}

	public void setCpfDestino(String cpfDestino) {
		this.cpfDestino = cpfDestino;
	}

	public String getNomeContraparte() {
		return nomeContraparte;
	}

	public void setNomeContraparte(String nomeContraparte) {
		this.nomeContraparte = nomeContraparte;
	}

	public String getDetalhe() {
		return detalhe;
	}

	public void setDetalhe(String detalhe) {
		this.detalhe = detalhe;
	}
}