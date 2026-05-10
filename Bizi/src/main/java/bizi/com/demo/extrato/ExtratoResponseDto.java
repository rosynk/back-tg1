package bizi.com.demo.extrato;

import java.math.BigDecimal;
import java.util.List;

public class ExtratoResponseDto {
    private String titular;
    private BigDecimal saldoAtual;
    private List<ExtratoDto> transacoes;

    public ExtratoResponseDto(String titular, BigDecimal saldoAtual, List<ExtratoDto> transacoes) {
        this.titular = titular;
        this.saldoAtual = saldoAtual;
        this.transacoes = transacoes;
    }

    // Getters
    public String getTitular() { return titular; }
    public BigDecimal getSaldoAtual() { return saldoAtual; }
    public List<ExtratoDto> getTransacoes() { return transacoes; }
}