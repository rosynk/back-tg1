package bizi.com.demo.extrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExtratoDto {
    private LocalDateTime data;
    private String tipo;
    private BigDecimal valor;
    private String detalhes;
    private String nomeContraparte;

    public ExtratoDto(LocalDateTime data, String tipo, BigDecimal valor, String detalhes, String nomeContraparte) {
        this.data = data;
        this.tipo = tipo;
        this.valor = valor;
        this.detalhes = detalhes;
        this.nomeContraparte = nomeContraparte;
    }

    // O Comparator vai usar este método aqui:
    public LocalDateTime getData() {
        return data;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public String getNomeContraparte() {
        return nomeContraparte;
    }
}