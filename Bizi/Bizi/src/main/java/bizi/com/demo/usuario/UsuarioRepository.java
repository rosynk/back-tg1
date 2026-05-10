package bizi.com.demo.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    /**
     * Busca um usuário pelo CPF.
     * Retorna Optional para ser usado no CustomUserDetailsService com
     * .orElseThrow()
     */
    Optional<UsuarioModel> findByCpf(String cpf);

    /**
     * Busca um usuário pelo email.
     * Útil para o fluxo de recuperação de senha.
     */
    Optional<UsuarioModel> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o CPF informado.
     * Usado para evitar duplicidade no cadastro.
     */
    boolean existsByCpf(String cpf);

    /**
     * Verifica se existe um usuário com o email informado.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se existe um usuário com o telefone informado.
     */
    boolean existsByTelefone(String telefone);

    /**
     * Busca por ID herdado do JpaRepository (Opcional declarar aqui, mas ajuda na
     * leitura)
     */
    Optional<UsuarioModel> findById(Long id);
}