package bizi.com.demo.usuario;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.logAcesso.LogAcessoModel;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class UsuarioModel implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @ManyToOne
    @JoinColumn(name = "endereco_id", nullable = true)
    private EnderecoModel endereco;

    @Column(nullable = false)
    private boolean ativo = false;

    @Column(unique = true)
    private String email;

    private String telefone;

    @Column(nullable = false)
    private String senha;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "codigo_recuperacao")
    private String codigoRecuperacao;

    @Column(name = "data_expiracao_codigo")
    private LocalDateTime dataExpiracaoCodigo;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<ContaBancariaModel> contas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<LogAcessoModel> logsAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private UsuarioModel responsavel;

    @OneToMany(mappedBy = "responsavel", cascade = CascadeType.ALL)
    private List<UsuarioModel> dependentes;

    @Column(name = "url_selfie")
    private String urlSelfie;

    @Column(name = "url_rg_frente")
    private String urlRgFrente;

    // --- MÉTODOS OBRIGATÓRIOS USERDETAILS (CORRIGIDOS) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ CORREÇÃO: Como o seu Enum já tem "ROLE_", usamos apenas o name().
        // Se usássemos "ROLE_" + this.role.name(), ficaria "ROLE_ROLE_ADMIN".
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        // ✅ AJUSTE: O CPF é a identidade de login no seu sistema.
        return this.cpf;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
    	return this.ativo; 
    }

    // --- CONSTRUTORES, GETTERS E SETTERS ---
    public UsuarioModel() {
    }

    public UsuarioModel(Long id, String nomeCompleto, String cpf, EnderecoModel endereco, String email,
            String telefone, String senha, LocalDateTime dataCadastro, Role role) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.endereco = endereco;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.dataCadastro = dataCadastro;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public EnderecoModel getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoModel endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<ContaBancariaModel> getContas() {
        return contas;
    }

    public void setContas(List<ContaBancariaModel> contas) {
        this.contas = contas;
    }

    public List<LogAcessoModel> getLogsAcesso() {
        return logsAcesso;
    }

    public void setLogsAcesso(List<LogAcessoModel> logsAcesso) {
        this.logsAcesso = logsAcesso;
    }

    public UsuarioModel getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(UsuarioModel responsavel) {
        this.responsavel = responsavel;
    }

    public List<UsuarioModel> getDependentes() {
        return dependentes;
    }

    public void setDependentes(List<UsuarioModel> dependentes) {
        this.dependentes = dependentes;
    }

    public String getCodigoRecuperacao() {
        return codigoRecuperacao;
    }

    public void setCodigoRecuperacao(String codigoRecuperacao) {
        this.codigoRecuperacao = codigoRecuperacao;
    }

    public LocalDateTime getDataExpiracaoCodigo() {
        return dataExpiracaoCodigo;
    }

    public void setDataExpiracaoCodigo(LocalDateTime dataExpiracaoCodigo) {
        this.dataExpiracaoCodigo = dataExpiracaoCodigo;
    }

    /**
     * @return boolean return the ativo
     */
    public boolean isAtivo() {
        return ativo;
    }

    /**
     * @param ativo the ativo to set
     */
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * @return String return the urlSelfie
     */
    public String getUrlSelfie() {
        return urlSelfie;
    }

    /**
     * @param urlSelfie the urlSelfie to set
     */
    public void setUrlSelfie(String urlSelfie) {
        this.urlSelfie = urlSelfie;
    }

    /**
     * @return String return the urlRgFrente
     */
    public String getUrlRgFrente() {
        return urlRgFrente;
    }

    /**
     * @param urlRgFrente the urlRgFrente to set
     */
    public void setUrlRgFrente(String urlRgFrente) {
        this.urlRgFrente = urlRgFrente;
    }

}