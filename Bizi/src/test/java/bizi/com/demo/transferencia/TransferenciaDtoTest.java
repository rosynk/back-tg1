package bizi.com.demo.transferencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransferenciaDtoTest {

    @Test
    @DisplayName("construtor vazio: deve permitir setar e obter todos os campos")
    void construtorVazio_devePermitirSettersEGetters() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 5, 19, 10, 30);

        TransferenciaDto dto = new TransferenciaDto();
        dto.setIdTransferencia(1L);
        dto.setContaOrigem(10L);
        dto.setAgenciaDestino("0001");
        dto.setNumeroContaDestino("12345-6");
        dto.setValor(new BigDecimal("250.00"));
        dto.setTipoTransferencia("TED");
        dto.setNomeOrigem("João Silva");
        dto.setNomeDestino("Maria Souza");
        dto.setDataHora(dataHora);
        dto.setStatus("CONCLUIDA");
        dto.setMensagem("Transferência TED realizada com sucesso.");

        assertThat(dto.getIdTransferencia()).isEqualTo(1L);
        assertThat(dto.getContaOrigem()).isEqualTo(10L);
        assertThat(dto.getAgenciaDestino()).isEqualTo("0001");
        assertThat(dto.getNumeroContaDestino()).isEqualTo("12345-6");
        assertThat(dto.getValor()).isEqualByComparingTo("250.00");
        assertThat(dto.getTipoTransferencia()).isEqualTo("TED");
        assertThat(dto.getNomeOrigem()).isEqualTo("João Silva");
        assertThat(dto.getNomeDestino()).isEqualTo("Maria Souza");
        assertThat(dto.getDataHora()).isEqualTo(dataHora);
        assertThat(dto.getStatus()).isEqualTo("CONCLUIDA");
        assertThat(dto.getMensagem()).isEqualTo("Transferência TED realizada com sucesso.");
    }

    @Test
    @DisplayName("construtor completo: deve preencher todos os campos")
    void construtorCompleto_devePreencherTodosOsCampos() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 5, 19, 11, 0);

        TransferenciaDto dto = new TransferenciaDto(
                1L,
                10L,
                "0001",
                "12345-6",
                new BigDecimal("250.00"),
                "DOC",
                "João Silva",
                "Maria Souza",
                dataHora,
                "CONCLUIDA",
                "Transferência DOC realizada com sucesso.");

        assertThat(dto.getIdTransferencia()).isEqualTo(1L);
        assertThat(dto.getContaOrigem()).isEqualTo(10L);
        assertThat(dto.getAgenciaDestino()).isEqualTo("0001");
        assertThat(dto.getNumeroContaDestino()).isEqualTo("12345-6");
        assertThat(dto.getValor()).isEqualByComparingTo("250.00");
        assertThat(dto.getTipoTransferencia()).isEqualTo("DOC");
        assertThat(dto.getNomeOrigem()).isEqualTo("João Silva");
        assertThat(dto.getNomeDestino()).isEqualTo("Maria Souza");
        assertThat(dto.getDataHora()).isEqualTo(dataHora);
        assertThat(dto.getStatus()).isEqualTo("CONCLUIDA");
        assertThat(dto.getMensagem()).isEqualTo("Transferência DOC realizada com sucesso.");
    }

    @Test
    @DisplayName("setters: devem aceitar valores nulos em campos read-only/opcionais")
    void setters_devemAceitarValoresNulos() {
        TransferenciaDto dto = new TransferenciaDto();

        dto.setIdTransferencia(null);
        dto.setContaOrigem(null);
        dto.setNomeOrigem(null);
        dto.setNomeDestino(null);
        dto.setDataHora(null);
        dto.setStatus(null);
        dto.setMensagem(null);

        assertThat(dto.getIdTransferencia()).isNull();
        assertThat(dto.getContaOrigem()).isNull();
        assertThat(dto.getNomeOrigem()).isNull();
        assertThat(dto.getNomeDestino()).isNull();
        assertThat(dto.getDataHora()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getMensagem()).isNull();
    }
}