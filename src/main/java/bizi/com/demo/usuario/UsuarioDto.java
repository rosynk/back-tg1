package bizi.com.demo.usuario;

import java.time.LocalDateTime;
import bizi.com.demo.endereco.EnderecoModel;
import jakarta.validation.constraints.*;

public class UsuarioDto {

    private Long id;

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos")
    private String cpf;

    private EnderecoModel endereco;

    @Email(message = "Email inválido")
    private String email;

    private String telefone;

    @NotBlank(message = "Senha obrigatória")
    private String senha;

    private Role role;

    // 🔥 NOVO CAMPO: Para vincular o dependente ao responsável
    private Long responsavelId;

    public UsuarioDto() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public EnderecoModel getEndereco() { return endereco; }
    public void setEndereco(EnderecoModel endereco) { this.endereco = endereco; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}