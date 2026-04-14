package bizi.com.demo.pagamentoBoleto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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