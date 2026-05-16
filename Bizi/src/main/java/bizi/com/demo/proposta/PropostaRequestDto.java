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
    private LocalDate dataNascimento;

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

    private String urlRgFrente;

    private String urlRgVerso;

    private String urlComprovanteResidencia;
    
    private String urlSelfie;

    public PropostaRequestDto() {
    }

    public String getUrlRgFrente() {
        return urlRgFrente;
    }

    public void setUrlRgFrente(String urlRgFrente) {
        this.urlRgFrente = urlRgFrente;
    }

    public String getUrlRgVerso() {
        return urlRgVerso;
    }

    public void setUrlRgVerso(String urlRgVerso) {
        this.urlRgVerso = urlRgVerso;
    }

    public String getUrlComprovanteResidencia() {
        return urlComprovanteResidencia;
    }

    public void setUrlComprovanteResidencia(String urlComprovanteResidencia) {
        this.urlComprovanteResidencia = urlComprovanteResidencia;
    }

    public String getUrlSelfie() {
        return urlSelfie;
    }

    public void setUrlSelfie(String urlSelfie) {
        this.urlSelfie = urlSelfie;
    }

    /**
     * @return String return the nomeCompleto
     */
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    /**
     * @param nomeCompleto the nomeCompleto to set
     */
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    /**
     * @return String return the cpf
     */
    public String getCpf() {
        return cpf;
    }

    /**
     * @param cpf the cpf to set
     */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /**
     * @return LocalDate return the dataNascimento
     */
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    /**
     * @param dataNascimento the dataNascimento to set
     */
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return String return the telefone
     */
    public String getTelefone() {
        return telefone;
    }

    /**
     * @param telefone the telefone to set
     */
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /**
     * @return String return the senha
     */
    public String getSenha() {
        return senha;
    }

    /**
     * @param senha the senha to set
     */
    public void setSenha(String senha) {
        this.senha = senha;
    }

    /**
     * @return EnderecoModel return the endereco
     */
    public EnderecoModel getEndereco() {
        return endereco;
    }

    /**
     * @param endereco the endereco to set
     */
    public void setEndereco(EnderecoModel endereco) {
        this.endereco = endereco;
    }

    /**
     * @return TipoConta return the tipoConta
     */
    public TipoConta getTipoConta() {
        return tipoConta;
    }

    /**
     * @param tipoConta the tipoConta to set
     */
    public void setTipoConta(TipoConta tipoConta) {
        this.tipoConta = tipoConta;
    }

    /**
     * @return Role return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

}