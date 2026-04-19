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

    // --- NOVOS CAMPOS PARA RECUPERAÇÃO DE SENHA (CORREÇÃO AQUI) ---
    
    @Column(name = "codigo_recuperacao")
    private String codigoRecuperacao;

    @Column(name = "data_expiracao_codigo")
    private LocalDateTime dataExpiracaoCodigo;

    // -----------------------------------------------------------

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

    // --- MÉTODOS OBRIGATÓRIOS USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() { return this.senha; }

    @Override
    public String getUsername() { return this.email; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // --- CONSTRUTORES ---

    public UsuarioModel() {}

    // Construtor atualizado para incluir os campos básicos
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

    // --- GETTERS E SETTERS ---

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

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<ContaBancariaModel> getContas() { return contas; }
    public void setContas(List<ContaBancariaModel> contas) { this.contas = contas; }

    public List<LogAcessoModel> getLogsAcesso() { return logsAcesso; }
    public void setLogsAcesso(List<LogAcessoModel> logsAcesso) { this.logsAcesso = logsAcesso; }

    public UsuarioModel getResponsavel() { return responsavel; }
    public void setResponsavel(UsuarioModel responsavel) { this.responsavel = responsavel; }

    public List<UsuarioModel> getDependentes() { return dependentes; }
    public void setDependentes(List<UsuarioModel> dependentes) { this.dependentes = dependentes; }

    // Getters e Setters dos novos campos
    public String getCodigoRecuperacao() { return codigoRecuperacao; }
    public void setCodigoRecuperacao(String codigoRecuperacao) { this.codigoRecuperacao = codigoRecuperacao; }

    public LocalDateTime getDataExpiracaoCodigo() { return dataExpiracaoCodigo; }
    public void setDataExpiracaoCodigo(LocalDateTime dataExpiracaoCodigo) { this.dataExpiracaoCodigo = dataExpiracaoCodigo; }
}