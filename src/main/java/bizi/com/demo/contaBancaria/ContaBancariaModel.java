package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bizi.com.demo.chavePix.ChavePixModel;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.usuario.UsuarioModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conta_bancaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaBancariaModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_numero_conta")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @Column(name = "numero_agencia", nullable = false)
    private String numeroAgencia;

    @Column(name = "tipo_conta", nullable = false)
    private String tipoConta;

    @Column(name = "status_conta")
    private Boolean statusConta = true;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "contaBancaria", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TransacaoModel> transacoes;

    @JsonIgnore
    @OneToMany(mappedBy = "contaBancaria", cascade = CascadeType.ALL)
    private List<ChavePixModel> chavesPix;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UsuarioModel getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioModel usuario) {
		this.usuario = usuario;
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

	public Boolean getStatusConta() {
		return statusConta;
	}

	public void setStatusConta(Boolean statusConta) {
		this.statusConta = statusConta;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public List<TransacaoModel> getTransacoes() {
		return transacoes;
	}

	public void setTransacoes(List<TransacaoModel> transacoes) {
		this.transacoes = transacoes;
	}

	public List<ChavePixModel> getChavesPix() {
		return chavesPix;
	}

	public void setChavesPix(List<ChavePixModel> chavesPix) {
		this.chavesPix = chavesPix;
	}
    
}
