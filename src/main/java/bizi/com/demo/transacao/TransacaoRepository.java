package bizi.com.demo.transacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransacaoRepository extends JpaRepository<TransacaoModel, Long> {
    
    List<TransacaoModel> findByContaBancariaId(Long idConta);
 
    List<TransacaoModel> findByTipoTransacao(String tipoTransacao);
    
    List<TransacaoModel> findByContaBancariaIdAndTipoTransacao(Long idConta, String tipoTransacao);

    List<TransacaoModel> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    List<TransacaoModel> findByContaBancariaIdAndDataHoraBetween(
        Long idConta, LocalDateTime dataInicio, LocalDateTime dataFim);

    List<TransacaoModel> findByContaBancariaIdOrderByDataHoraDesc(Long idConta);
}