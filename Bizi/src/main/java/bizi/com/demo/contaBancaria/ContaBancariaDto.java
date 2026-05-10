package bizi.com.demo.contaBancaria;

import jakarta.validation.constraints.NotNull;

public class ContaBancariaDto {

    private Long id;
    
    // Mantido para o fluxo de PropostaService onde o usuário ainda não logou
    private Long usuarioId; 

    @NotNull(message = "O tipo de conta é obrigatório")
    private TipoConta tipoConta; 

    private String numeroAgencia;
    private String numeroConta;

    public ContaBancariaDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public TipoConta getTipoConta() { return tipoConta; }
    public void setTipoConta(TipoConta tipoConta) { this.tipoConta = tipoConta; }

    public String getNumeroAgencia() { return numeroAgencia; }
    public void setNumeroAgencia(String numeroAgencia) { this.numeroAgencia = numeroAgencia; }

    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
}