package bizi.com.demo.chavePix;

import java.time.LocalDateTime;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chave_pix")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChavePixModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chave_pix")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_numero_conta", nullable = false)
    private ContaBancariaModel contaBancaria;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    private String chave;

    @Column(name = "tipo_chave", nullable = false)
    private String tipoChave;
    
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

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public String getTipoChave() {
		return tipoChave;
	}

	public void setTipoChave(String tipoChave) {
		this.tipoChave = tipoChave;
	}

}
