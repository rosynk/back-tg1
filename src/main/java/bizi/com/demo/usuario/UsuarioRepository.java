package bizi.com.demo.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    
    /**
     * Busca um usuário pelo CPF
     * @param cpf CPF do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<UsuarioModel> findByCpf(String cpf);
    
    /**
     * Verifica se existe um usuário com o CPF informado
     * @param cpf CPF do usuário
     * @return true se existe, false caso contrário
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se existe um usuário com o email informado
     * @param email Email do usuário
     * @return true se existe, false caso contrário
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca um usuário pelo email
     * @param email Email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<UsuarioModel> findByEmail(String email);
}
