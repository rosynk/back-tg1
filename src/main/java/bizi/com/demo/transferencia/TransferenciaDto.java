package bizi.com.demo.transferencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaDto {
	
    private Long idTransferencia;
	private Long idTransacao;
    private Long contaOrigem;
    private String nomeOrigem;
    private Long contaDestino;
    private String nomeDestino;
    private String agenciaDestino;
    private BigDecimal valor;
    private String status;
    private LocalDateTime dataHora;
    private String mensagem;
    
    public Long getIdTransferencia() {
		return idTransferencia;
	}
	public void setIdTransferencia(Long idTransferencia) {
		this.idTransferencia = idTransferencia;
	}
	public Long getIdTransacao() {
		return idTransacao;
	}
	public void setIdTransacao(Long idTransacao) {
		this.idTransacao = idTransacao;
	}
	public Long getContaOrigem() {
		return contaOrigem;
	}
	public void setContaOrigem(Long contaOrigem) {
		this.contaOrigem = contaOrigem;
	}
	public String getNomeOrigem() {
		return nomeOrigem;
	}
	public void setNomeOrigem(String nomeOrigem) {
		this.nomeOrigem = nomeOrigem;
	}
	public Long getContaDestino() {
		return contaDestino;
	}
	public void setContaDestino(Long contaDestino) {
		this.contaDestino = contaDestino;
	}
	public String getNomeDestino() {
		return nomeDestino;
	}
	public void setNomeDestino(String nomeDestino) {
		this.nomeDestino = nomeDestino;
	}
	public String getAgenciaDestino() {
		return agenciaDestino;
	}
	public void setAgenciaDestino(String agenciaDestino) {
		this.agenciaDestino = agenciaDestino;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getDataHora() {
		return dataHora;
	}
	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
    
}