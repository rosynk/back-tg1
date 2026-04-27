package bizi.com.demo.proposta;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "propostas")
public class PropostaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cpf;

    private String nomeCompleto;
    private StatusProposta status;
    private String observacao;
    private Integer scoreNoMomento; // Guardamos o score que foi "mockado" na análise
    private LocalDateTime dataCriacao;

    public PropostaModel() {}

    public PropostaModel(String cpf, String nomeCompleto, StatusProposta status, String observacao, Integer scoreNoMomento, LocalDateTime dataCriacao) {
        this.cpf = cpf;
        this.nomeCompleto = nomeCompleto;
        this.status = status;
        this.observacao = observacao;
        this.scoreNoMomento = scoreNoMomento;
        this.dataCriacao = dataCriacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public StatusProposta getStatus() { return status; }
    public void setStatus(StatusProposta status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Integer getScoreNoMomento() { return scoreNoMomento; }
    public void setScoreNoMomento(Integer scoreNoMomento) { this.scoreNoMomento = scoreNoMomento; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}