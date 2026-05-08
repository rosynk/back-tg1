package bizi.com.demo.proposta;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaRepository extends JpaRepository<PropostaModel, Long> {
    boolean existsByCpf(String cpf);
}