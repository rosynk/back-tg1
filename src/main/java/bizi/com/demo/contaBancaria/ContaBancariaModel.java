package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bizi.com.demo.chavePix.ChavePixModel;
import bizi.com.demo.transacao.TransacaoModel;
import bizi.com.demo.usuario.UsuarioModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conta_bancaria")
@NoArgsConstructor
@AllArgsConstructor
public class ContaBancariaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_numero_conta")
    private Long id;

    // 🔥 ADICIONADO: O número da conta que o cliente realmente usa
    @Column(name = "numero_conta", nullable = false, unique = true)
    private String numeroConta;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @Column(name = "numero_agencia", nullable = false)
    private String numeroAgencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false)
    private TipoConta tipoConta;

    @Column(name = "status_conta")
    private Boolean statusConta = true;

    // 🔥 AJUSTE: Garante que nunca seja nulo e comece em ZERO
    @Column(nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "contaBancaria", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TransacaoModel> transacoes;

    @JsonIgnore
    @OneToMany(mappedBy = "contaBancaria", cascade = CascadeType.ALL)
    private List<ChavePixModel> chavesPix;

    // --- GETTERS E SETTERS (Adicione o do numeroConta) ---

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

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

    public TipoConta getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(TipoConta tipoConta) {
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