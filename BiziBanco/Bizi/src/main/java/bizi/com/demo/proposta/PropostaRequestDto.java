package bizi.com.demo.proposta;

import bizi.com.demo.contaBancaria.TipoConta;
import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.usuario.Role;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PropostaRequestDto {

    @NotBlank(message = "Nome é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento; // Campo necessário para a validação de idade

    @Email(message = "Email inválido")
    private String email;

    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @NotNull(message = "Dados de endereço são obrigatórios")
    private EnderecoModel endereco;

    @NotNull(message = "Tipo de conta é obrigatório")
    private TipoConta tipoConta;

    private Role role;

    public PropostaRequestDto() {}

    // Getters e Setters
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public EnderecoModel getEndereco() { return endereco; }
    public void setEndereco(EnderecoModel endereco) { this.endereco = endereco; }

    public TipoConta getTipoConta() { return tipoConta; }
    public void setTipoConta(TipoConta tipoConta) { this.tipoConta = tipoConta; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}