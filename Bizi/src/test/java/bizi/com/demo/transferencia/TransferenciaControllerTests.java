package bizi.com.demo.transferencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TransferenciaControllerTest {

    @Mock
    private TransferenciaService transferenciaService;

    @InjectMocks
    private TransferenciaController controller;

    @Test
    @DisplayName("realizarTransferencia: deve retornar 201 com recibo")
    void realizarTransferencia_deveRetornar201ComRecibo() {
        TransferenciaDto dto = new TransferenciaDto();
        TransferenciaDto recibo = new TransferenciaDto();
        recibo.setStatus("CONCLUIDA");

        when(transferenciaService.realizarTransferencia(dto)).thenReturn(recibo);

        ResponseEntity<TransferenciaDto> response = controller.realizarTransferencia(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(recibo);
        assertThat(response.getBody().getStatus()).isEqualTo("CONCLUIDA");
    }

    @Test
    @DisplayName("buscarPorId: deve retornar 200 com transferencia")
    void buscarPorId_deveRetornar200ComTransferencia() {
        TransferenciaModel transferencia = new TransferenciaModel();

        when(transferenciaService.buscarPorId(1L)).thenReturn(transferencia);

        ResponseEntity<TransferenciaModel> response = controller.buscarPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(transferencia);
    }

    @Test
    @DisplayName("buscarTodasDaConta: deve retornar 200 com lista")
    void buscarTodasDaConta_deveRetornar200ComLista() {
        List<TransferenciaModel> transferencias = List.of(new TransferenciaModel(), new TransferenciaModel());

        when(transferenciaService.buscarTodasDaConta(1L)).thenReturn(transferencias);

        ResponseEntity<List<TransferenciaModel>> response = controller.buscarTodasDaConta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("exportarPdf: deve retornar arquivo PDF com headers corretos")
    void exportarPdf_deveRetornarPdfComHeaders() {
        byte[] pdf = "%PDF-1.4".getBytes(StandardCharsets.UTF_8);

        when(transferenciaService.gerarPdfExtrato(1L)).thenReturn(pdf);

        ResponseEntity<byte[]> response = controller.exportarPdf(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdf);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("extrato_bizi_1.pdf");
    }

    @Test
    @DisplayName("exportarCsv: deve retornar arquivo CSV com headers corretos")
    void exportarCsv_deveRetornarCsvComHeaders() {
        byte[] csv = "ID;Data;Tipo Transferencia\n".getBytes(StandardCharsets.UTF_8);

        when(transferenciaService.gerarCsvExtrato(1L)).thenReturn(csv);

        ResponseEntity<byte[]> response = controller.exportarCsv(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(csv);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("extrato_bizi_1.csv");
    }

    @Test
    @DisplayName("estornar: deve retornar 204 quando estorno realizado")
    void estornar_deveRetornar204() {
        doNothing().when(transferenciaService).estornarTransferencia(1L);

        ResponseEntity<Void> response = controller.estornar(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }
    
    @Test
    @DisplayName("realizarTransferencia: deve propagar exceção quando service falha")
    void realizarTransferencia_devePropagarExcecaoQuandoServiceFalha() {
        TransferenciaDto dto = new TransferenciaDto();

        when(transferenciaService.realizarTransferencia(dto))
                .thenThrow(new RuntimeException("Erro na transferência"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.realizarTransferencia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro na transferência");
    }

    @Test
    @DisplayName("buscarPorId: deve propagar exceção quando transferência não existe")
    void buscarPorId_devePropagarExcecaoQuandoNaoExiste() {
        when(transferenciaService.buscarPorId(99L))
                .thenThrow(new RuntimeException("Transferência não encontrada."));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transferência não encontrada");
    }

    @Test
    @DisplayName("buscarTodasDaConta: deve retornar lista vazia")
    void buscarTodasDaConta_deveRetornarListaVazia() {
        when(transferenciaService.buscarTodasDaConta(1L)).thenReturn(List.of());

        ResponseEntity<List<TransferenciaModel>> response = controller.buscarTodasDaConta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("exportarPdf: deve retornar PDF vazio quando service retorna bytes vazios")
    void exportarPdf_deveRetornarBytesVazios() {
        byte[] pdf = new byte[0];

        when(transferenciaService.gerarPdfExtrato(1L)).thenReturn(pdf);

        ResponseEntity<byte[]> response = controller.exportarPdf(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("extrato_bizi_1.pdf");
    }

    @Test
    @DisplayName("exportarCsv: deve retornar CSV vazio quando service retorna bytes vazios")
    void exportarCsv_deveRetornarBytesVazios() {
        byte[] csv = new byte[0];

        when(transferenciaService.gerarCsvExtrato(1L)).thenReturn(csv);

        ResponseEntity<byte[]> response = controller.exportarCsv(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("extrato_bizi_1.csv");
    }

    @Test
    @DisplayName("estornar: deve propagar exceção quando service falha")
    void estornar_devePropagarExcecaoQuandoServiceFalha() {
        org.mockito.Mockito.doThrow(new RuntimeException("Transferência não localizada"))
                .when(transferenciaService).estornarTransferencia(99L);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.estornar(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transferência não localizada");
    }
}