package bizi.com.demo.transferencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransferenciaRepositoryTest {

    @Test
    @DisplayName("findByContaOrigem: deve delegar para findByTransacaoContaBancariaId")
    void findByContaOrigem_deveDelegarParaFindByTransacaoContaBancariaId() {
        TransferenciaRepository repository = Mockito.mock(
                TransferenciaRepository.class,
                Mockito.CALLS_REAL_METHODS);

        TransferenciaModel transferencia = new TransferenciaModel();

        when(repository.findByTransacaoContaBancariaId(1L))
                .thenReturn(List.of(transferencia));

        List<TransferenciaModel> resultado = repository.findByContaOrigem(1L);

        assertThat(resultado).containsExactly(transferencia);
    }
}