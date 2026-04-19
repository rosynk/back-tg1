package bizi.com.demo.usuario;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.endereco.EnderecoRepository;
import bizi.com.demo.security.SecurityUtil;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Busca um usuário pelo ID
     */
    @Transactional(readOnly = true)
    public UsuarioModel buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
    }

    /**
     * MÉTODO ÚNICO: Cria um novo usuário com regras de Role e Hierarquia
     */
    /**
     * MÉTODO ÚNICO: Cria um novo usuário com regras de Role e Hierarquia
     */
    public UsuarioModel criarUsuario(UsuarioDto usuarioDto) {
        
        // 1. Validações de duplicidade
        if (usuarioRepository.existsByCpf(usuarioDto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado!");
        }

        // 2. Lógica de Hierarquia e Roles
        Role roleSolicitado = usuarioDto.getRole();
        UsuarioModel responsavel = null;

        try {
            // Tenta pegar quem está logado para validar permissões
            Role roleLogado = securityUtil.getRoleUsuarioLogado();

            // Bloqueio: Cliente comum não pode criar Admin
            if (roleLogado == Role.ROLE_CLIENTE && roleSolicitado == Role.ROLE_ADMIN) {
                throw new RuntimeException("Clientes não possuem permissão para criar administradores!");
            }
            
            // Se um responsavelId foi enviado (caso do filho), buscamos o pai no banco
            if (usuarioDto.getResponsavelId() != null) {
                responsavel = usuarioRepository.findById(usuarioDto.getResponsavelId())
                        .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
            }

        } catch (Exception e) {
            // Caso de AUTO-CADASTRO (Ninguém logado no sistema ainda)
            // Aqui mudei de ROLE_USER para ROLE_CLIENTE para bater com seu Enum!
            if (roleSolicitado == null) roleSolicitado = Role.ROLE_CLIENTE; 
        }

        // 3. Persistência do Endereço
        EnderecoModel endereco = null;
        if (usuarioDto.getEndereco() != null) {
            endereco = enderecoRepository.save(usuarioDto.getEndereco());
        }

        // 4. Mapeamento para a Model
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNomeCompleto(usuarioDto.getNomeCompleto());
        usuario.setCpf(usuarioDto.getCpf());
        usuario.setEndereco(endereco);
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setTelefone(usuarioDto.getTelefone());
        usuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        usuario.setDataCadastro(LocalDateTime.now());
        usuario.setRole(roleSolicitado);
        
        // Vincula o Pai/Responsável se houver
        if (responsavel != null) {
            usuario.setResponsavel(responsavel);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Busca um usuário pelo CPF
     */
    @Transactional(readOnly = true)
    public UsuarioModel buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com CPF: " + cpf));
    }

    /**
     * Lista todos os usuários
     */
    @Transactional(readOnly = true)
    public List<UsuarioModel> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Atualiza um usuário existente
     */
    public UsuarioModel atualizarUsuario(Long id, UsuarioDto usuarioDto) {
        UsuarioModel usuarioExistente = buscarPorId(id);
        
        // Validar se o novo CPF já pertence a outra pessoa
        if (usuarioDto.getCpf() != null && !usuarioExistente.getCpf().equals(usuarioDto.getCpf()) 
            && usuarioRepository.existsByCpf(usuarioDto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado em outro usuário");
        }
        
        // Atualizar endereço se fornecido
        if (usuarioDto.getEndereco() != null) {
            EnderecoModel enderecoSalvo = enderecoRepository.save(usuarioDto.getEndereco());
            usuarioExistente.setEndereco(enderecoSalvo);
        }
        
        if (usuarioDto.getNomeCompleto() != null) usuarioExistente.setNomeCompleto(usuarioDto.getNomeCompleto());
        if (usuarioDto.getCpf() != null) usuarioExistente.setCpf(usuarioDto.getCpf());
        if (usuarioDto.getEmail() != null) usuarioExistente.setEmail(usuarioDto.getEmail());
        if (usuarioDto.getTelefone() != null) usuarioExistente.setTelefone(usuarioDto.getTelefone());
        
        if (usuarioDto.getRole() != null) {
            usuarioExistente.setRole(usuarioDto.getRole());
        }
        
        if (usuarioDto.getSenha() != null && !usuarioDto.getSenha().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        }
        
        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Deleta um usuário por ID
     */
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Deleta um usuário pelo CPF
     */
    public void deletarUsuarioPorCpf(String cpf) {
        UsuarioModel usuario = buscarPorCpf(cpf);
        usuarioRepository.deleteById(usuario.getId());
    }
}