package bizi.com.demo.pix;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PixDto {

    @NotBlank(message = "A chave Pix de destino é obrigatória")
    private String chavePixDestino;

    @NotNull(message = "O valor não pode ser nulo")
    @Positive(message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    private String mensagem; // Opcional

    // --- Getters e Setters ---
    public String getChavePixDestino() { return chavePixDestino; }
    public void setChavePixDestino(String chavePixDestino) { this.chavePixDestino = chavePixDestino; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}