package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaBancariaDto {

    @NotNull(message = "ID do usuário é obrigatório")
    private Long idUsuario;

    @NotBlank(message = "Número da agência é obrigatório")
    @Pattern(regexp = "\\d{4}", message = "Número da agência deve conter 4 dígitos")
    private String numeroAgencia;

    @NotBlank(message = "Tipo de conta é obrigatório")
   // @Pattern(regexp = "CORRENTE|POUPANCA|SALARIO", message = "Tipo de conta deve ser: CORRENTE, POUPANCA ou SALARIO")
    private String tipoConta;

    @PositiveOrZero(message = "Saldo deve ser zero ou positivo")
    private BigDecimal saldo;
    
    public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNumeroAgencia() {
		return numeroAgencia;
	}

	public void setNumeroAgencia(String numeroAgencia) {
		this.numeroAgencia = numeroAgencia;
	}

	public String getTipoConta() {
		return tipoConta;
	}

	public void setTipoConta(String tipoConta) {
		this.tipoConta = tipoConta;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
}