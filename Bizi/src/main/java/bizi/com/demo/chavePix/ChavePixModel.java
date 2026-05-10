package bizi.com.demo.chavePix;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import bizi.com.demo.contaBancaria.ContaBancariaModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chave_pix")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChavePixModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chave_pix")
    private Long id;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @JsonProperty("chave")
    private String valor;

    @Column(name = "tipo_chave", nullable = false)
    private String tipoChave;

    // CORREÇÃO: Nome alterado de 'conta' para 'contaBancaria' para bater com o mappedBy
    @ManyToOne
    @JoinColumn(name = "id_conta")
    private ContaBancariaModel contaBancaria;
    
   
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getTipoChave() { return tipoChave; }
    public void setTipoChave(String tipoChave) { this.tipoChave = tipoChave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    // GETTER E SETTER CORRIGIDOS PARA O NOVO NOME
    public ContaBancariaModel getConta() { return contaBancaria; }
    public void setConta(ContaBancariaModel contaBancaria) { this.contaBancaria = contaBancaria; }
}