package bizi.com.demo.transferencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferenciaDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long idTransferencia;

    @Schema(description = "ID da conta de origem (Obrigatório apenas para ADMIN)", example = "1")
    private Long contaOrigem;

    @NotBlank(message = "Agência de destino é obrigatória")
    @Schema(description = "Código da agência do destinatário", example = "0001")
    private String agenciaDestino;

    @NotBlank(message = "Conta de destino é obrigatória")
    @Schema(description = "Número da conta do destinatário", example = "12345-6")
    private String numeroContaDestino;

    @NotNull(message = "O valor não pode ser nulo")
    @Positive(message = "O valor deve ser maior que zero")
    @Schema(description = "Valor da transferência", example = "250.00")
    private BigDecimal valor;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String nomeOrigem;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String nomeDestino;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataHora;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "CONCLUIDA")
    private String status;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String mensagem;

    // --- CONSTRUTORES ---

    // Construtor Padrão (Necessário para o Jackson/Spring)
    public TransferenciaDto() {
    }

    // Construtor Completo
    public TransferenciaDto(Long idTransferencia, Long contaOrigem, String agenciaDestino, String numeroContaDestino, 
                            BigDecimal valor, String nomeOrigem, String nomeDestino, LocalDateTime dataHora, 
                            String status, String mensagem) {
        this.idTransferencia = idTransferencia;
        this.contaOrigem = contaOrigem;
        this.agenciaDestino = agenciaDestino;
        this.numeroContaDestino = numeroContaDestino;
        this.valor = valor;
        this.nomeOrigem = nomeOrigem;
        this.nomeDestino = nomeDestino;
        this.dataHora = dataHora;
        this.status = status;
        this.mensagem = mensagem;
    }

    // --- GETTERS E SETTERS ---

    public Long getIdTransferencia() {
        return idTransferencia;
    }

    public void setIdTransferencia(Long idTransferencia) {
        this.idTransferencia = idTransferencia;
    }

    public Long getContaOrigem() {
        return contaOrigem;
    }

    public void setContaOrigem(Long contaOrigem) {
        this.contaOrigem = contaOrigem;
    }

    public String getAgenciaDestino() {
        return agenciaDestino;
    }

    public void setAgenciaDestino(String agenciaDestino) {
        this.agenciaDestino = agenciaDestino;
    }

    public String getNumeroContaDestino() {
        return numeroContaDestino;
    }

    public void setNumeroContaDestino(String numeroContaDestino) {
        this.numeroContaDestino = numeroContaDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getNomeOrigem() {
        return nomeOrigem;
    }

    public void setNomeOrigem(String nomeOrigem) {
        this.nomeOrigem = nomeOrigem;
    }

    public String getNomeDestino() {
        return nomeDestino;
    }

    public void setNomeDestino(String nomeDestino) {
        this.nomeDestino = nomeDestino;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}