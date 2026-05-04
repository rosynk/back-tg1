package bizi.com.demo.transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import jakarta.persistence.*;

@Entity
@Table(name = "transacao")
public class TransacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transacao")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_numero_conta", nullable = false)
    private ContaBancariaModel contaBancaria;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transacao", nullable = false)
    private TipoTransacao tipoTransacao;

    // 🔥 NOVOS CAMPOS PARA SUPORTAR A LÓGICA DE CPF DO TOKEN
    @Column(name = "cpf_origem")
    private String cpfOrigem;

    @Column(name = "cpf_destino")
    private String cpfDestino;

    @Column(name = "nome_contraparte")
    private String nomeContraparte;

    @Column(name = "detalhe")
    private String detalhe;

    // Construtores
    public TransacaoModel() {
    }

    public TransacaoModel(Long id, ContaBancariaModel contaBancaria, BigDecimal valor,
            LocalDateTime dataHora, TipoTransacao tipoTransacao,
            String cpfOrigem, String cpfDestino, String nomeContraparte, String detalhe) {
        this.id = id;
        this.contaBancaria = contaBancaria;
        this.valor = valor;
        this.dataHora = dataHora;
        this.tipoTransacao = tipoTransacao;
        this.cpfOrigem = cpfOrigem;
        this.cpfDestino = cpfDestino;
        this.nomeContraparte = nomeContraparte;
        this.detalhe = detalhe;
    }

    // --- Getters e Setters (Mantendo os antigos e adicionando os novos) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContaBancariaModel getContaBancaria() {
        return contaBancaria;
    }

    public void setContaBancaria(ContaBancariaModel contaBancaria) {
        this.contaBancaria = contaBancaria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public TipoTransacao getTipoTransacao() {
        return tipoTransacao;
    }

    public void setTipoTransacao(TipoTransacao tipoTransacao) {
        this.tipoTransacao = tipoTransacao;
    }

    // 🔥 NOVOS GETTERS E SETTERS
    public String getCpfOrigem() {
        return cpfOrigem;
    }

    public void setCpfOrigem(String cpfOrigem) {
        this.cpfOrigem = cpfOrigem;
    }

    public String getCpfDestino() {
        return cpfDestino;
    }

    public void setCpfDestino(String cpfDestino) {
        this.cpfDestino = cpfDestino;
    }

    /**
     * @return String return the nomeContraparte
     */
    public String getNomeContraparte() {
        return nomeContraparte;
    }

    /**
     * @param nomeContraparte the nomeContraparte to set
     */
    public void setNomeContraparte(String nomeContraparte) {
        this.nomeContraparte = nomeContraparte;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public void setDetalhe(String detalhe) {
        this.detalhe = detalhe;
    }

}