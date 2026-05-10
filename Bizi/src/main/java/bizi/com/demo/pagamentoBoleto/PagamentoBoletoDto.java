package bizi.com.demo.pagamentoBoleto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoBoletoDto {

    private Long idPagamento;
    private Long idTransacao;
    private Long contaOrigem;
    private String nomeOrigem;
    private String codigoBarras;
    private String agBeneficiario;
    private String contaBeneficiario;
    private BigDecimal valor;
    private String status;
    private LocalDateTime dataHora;
    private String mensagem;
    private String nomeBeneficiario;
    
    public String getNomeBeneficiario() {
		return nomeBeneficiario;
	}

	public void setNomeBeneficiario(String nomeBeneficiario) {
		this.nomeBeneficiario = nomeBeneficiario;
	}

	public PagamentoBoletoDto(PagamentoBoletoModel model) {
        this.idPagamento = model.getId();
        this.idTransacao = model.getTransacao().getId();
        this.contaOrigem = model.getTransacao().getContaBancaria().getId();
        this.nomeOrigem = model.getTransacao().getContaBancaria().getUsuario().getNomeCompleto();
        this.codigoBarras = model.getCodigoBarras();
        this.nomeBeneficiario = model.getNomeBeneficiario(); // Adicione esta linha
        this.valor = model.getTransacao().getValor();
        this.dataHora = model.getTransacao().getDataHora();
        this.status = "CONCLUIDO";
    }

    public Long getIdPagamento() { return idPagamento; }
    public void setIdPagamento(Long idPagamento) { this.idPagamento = idPagamento; }

    public Long getIdTransacao() { return idTransacao; }
    public void setIdTransacao(Long idTransacao) { this.idTransacao = idTransacao; }

    public Long getContaOrigem() { return contaOrigem; }
    public void setContaOrigem(Long contaOrigem) { this.contaOrigem = contaOrigem; }

    public String getNomeOrigem() { return nomeOrigem; }
    public void setNomeOrigem(String nomeOrigem) { this.nomeOrigem = nomeOrigem; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getAgBeneficiario() { return agBeneficiario; }
    public void setAgBeneficiario(String agBeneficiario) { this.agBeneficiario = agBeneficiario; }

    public String getContaBeneficiario() { return contaBeneficiario; }
    public void setContaBeneficiario(String contaBeneficiario) { this.contaBeneficiario = contaBeneficiario; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}