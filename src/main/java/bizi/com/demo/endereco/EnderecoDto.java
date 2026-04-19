package bizi.com.demo.endereco;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class EnderecoDto {

    @Schema(hidden = true) // Isso faz o ID sumir do exemplo do Swagger
    private Long id;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter exatamente 8 dígitos")
    @Schema(example = "01001000", description = "CEP apenas números")
    private String cep;

    @NotNull(message = "Número é obrigatório")
    @Schema(example = "123")
    private Integer numero;

    @Schema(example = "Apto 42", description = "Opcional")
    @Size(max = 50, message = "Complemento deve ter no máximo 50 caracteres")
    private String complemento;

    // Campos abaixo marcados como hidden=true ou readOnly=true no Swagger
    // pois o ViaCEP preencherá automaticamente no Service.
    
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) 
    private String rua;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String bairro;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String cidade;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String estado;

    // Getters e Setters (Mantidos para o Spring conseguir trabalhar)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
}