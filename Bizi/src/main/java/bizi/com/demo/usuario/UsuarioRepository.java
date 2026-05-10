package bizi.com.demo.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    
    /**
     * Busca um usuário pelo CPF
     */
    Optional<UsuarioModel> findByCpf(String cpf);
    
    /**
     * Verifica se existe um usuário com o CPF informado
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se existe um usuário com o email informado
     */
    boolean existsByEmail(String email);
    
    /**
     * NOVO: Verifica se existe um usuário com o telefone informado
     * Essencial para a trava de cadastro único que discutimos.
     */
    boolean existsByTelefone(String telefone);
    
    /**
     * Busca um usuário pelo email
     */
    Optional<UsuarioModel> findByEmail(String email);
    
    Optional<UsuarioModel> findById(Long id);
}