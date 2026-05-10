package bizi.com.demo.proposta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaRepository extends JpaRepository<PropostaModel, Long> {
    boolean existsByCpf(String cpf);

    List<PropostaModel> findByStatus(StatusProposta status);
}