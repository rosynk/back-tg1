package bizi.com.demo.extrato;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExtratoDto {
    private LocalDateTime data;
    private String tipo;
    private BigDecimal valor;
    private String detalhes;

    public ExtratoDto(LocalDateTime data, String tipo, BigDecimal valor, String detalhes) {
        this.data = data;
        this.tipo = tipo;
        this.valor = valor;
        this.detalhes = detalhes;
    }

    // O Comparator vai usar este método aqui:
    public LocalDateTime getData() {
        return data;
    }

    public String getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public String getDetalhes() { return detalhes; }
}