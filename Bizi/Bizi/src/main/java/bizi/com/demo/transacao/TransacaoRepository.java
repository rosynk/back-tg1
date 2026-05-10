package bizi.com.demo.transacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransacaoRepository extends JpaRepository<TransacaoModel, Long> {

    List<TransacaoModel> findByContaBancariaId(Long idConta);

    List<TransacaoModel> findByTipoTransacao(TipoTransacao tipoTransacao); // Use o Enum se TipoTransacao for um Enum

    List<TransacaoModel> findByContaBancariaIdAndTipoTransacao(Long idConta, TipoTransacao tipoTransacao);

    List<TransacaoModel> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    List<TransacaoModel> findByContaBancariaIdAndDataHoraBetween(
            Long idConta, LocalDateTime dataInicio, LocalDateTime dataFim);

    List<TransacaoModel> findByContaBancariaIdOrderByDataHoraDesc(Long idConta);

    @Query("SELECT t FROM TransacaoModel t WHERE t.cpfOrigem = :cpf OR t.cpfDestino = :cpf")
    List<TransacaoModel> findByCpfParaExtrato(@Param("cpf") String cpf);

    // CORRIGIDO: O retorno deve ser List<TransacaoModel>
    // O Spring agora vai conseguir mapear corretamente para o seu banco
    List<TransacaoModel> findByContaBancariaIdAndDataHoraAfter(Long idConta, LocalDateTime data);
}