package bizi.com.demo.usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.endereco.EnderecoRepository;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EnderecoRepository enderecoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Cria um novo usuário
     * @param usuarioDto Dados do usuário
     * @return UsuarioModel criado
     */
    public UsuarioModel criarUsuario(UsuarioDto usuarioDto) {
        // Validar se CPF já existe
        if (usuarioRepository.existsByCpf(usuarioDto.getCpf())) {
            throw new UsuarioConflictException("CPF já cadastrado: " + usuarioDto.getCpf());
        }
        
        // Validar se email já existe (se fornecido)
        if (usuarioDto.getEmail() != null && !usuarioDto.getEmail().isEmpty() 
            && usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new UsuarioConflictException("Email já cadastrado: " + usuarioDto.getEmail());
        }
        
        // IMPORTANTE: Salvar o endereço PRIMEIRO
        EnderecoModel endereco = enderecoRepository.save(usuarioDto.getEndereco());
        
        // Criar o usuário com o endereço salvo
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNomeCompleto(usuarioDto.getNomeCompleto());
        usuario.setCpf(usuarioDto.getCpf());
        usuario.setEndereco(endereco);  // Usar o endereço com ID
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setTelefone(usuarioDto.getTelefone());
        usuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        usuario.setDataCadastro(LocalDateTime.now());
        usuario.setTipoUsuario(usuarioDto.getTipoUsuario());

        return usuarioRepository.save(usuario);
    }

    /**
     * Busca um usuário pelo CPF
     * @param cpf CPF do usuário
     * @return UsuarioModel se encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioModel buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com CPF: " + cpf));
    }

    /**
     * Busca um usuário pelo ID
     * @param id ID do usuário
     * @return UsuarioModel se encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioModel buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com ID: " + id));
    }

    /**
     * Lista todos os usuários
     * @return Lista de usuários
     */
    @Transactional(readOnly = true)
    public List<UsuarioModel> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Atualiza um usuário existente
     * @param id ID do usuário
     * @param usuarioDto Dados atualizados
     * @return UsuarioModel atualizado
     */
    public UsuarioModel atualizarUsuario(Long id, UsuarioDto usuarioDto) {
        UsuarioModel usuarioExistente = buscarPorId(id);
        
        // Validar se CPF já existe em outro usuário
        if (!usuarioExistente.getCpf().equals(usuarioDto.getCpf()) 
            && usuarioRepository.existsByCpf(usuarioDto.getCpf())) {
            throw new UsuarioConflictException("CPF já cadastrado: " + usuarioDto.getCpf());
        }
        
        // Validar se email já existe em outro usuário (se fornecido)
        if (usuarioDto.getEmail() != null && !usuarioDto.getEmail().isEmpty()) {
            Optional<UsuarioModel> usuarioComEmail = usuarioRepository.findByEmail(usuarioDto.getEmail());
            if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(id)) {
                throw new UsuarioConflictException("Email já cadastrado: " + usuarioDto.getEmail());
            }
        }
        
        // Atualizar endereço se fornecido
        if (usuarioDto.getEndereco() != null) {
            EnderecoModel enderecoAtualizado = enderecoRepository.save(usuarioDto.getEndereco());
            usuarioExistente.setEndereco(enderecoAtualizado);
        }
        
        // Atualizar campos
        usuarioExistente.setNomeCompleto(usuarioDto.getNomeCompleto());
        usuarioExistente.setCpf(usuarioDto.getCpf());
        usuarioExistente.setEmail(usuarioDto.getEmail());
        usuarioExistente.setTelefone(usuarioDto.getTelefone());
        usuarioExistente.setTipoUsuario(usuarioDto.getTipoUsuario());
        
        // Atualizar senha apenas se fornecida
        if (usuarioDto.getSenha() != null && !usuarioDto.getSenha().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        }
        
        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Deleta um usuário
     * @param id ID do usuário
     */
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Deleta um usuário pelo CPF
     * @param cpf CPF do usuário
     */
    public void deletarUsuarioPorCpf(String cpf) {
        UsuarioModel usuario = buscarPorCpf(cpf);
        usuarioRepository.deleteById(usuario.getId());
    }
}