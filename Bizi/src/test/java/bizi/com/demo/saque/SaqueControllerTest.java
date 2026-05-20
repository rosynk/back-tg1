package bizi.com.demo.saque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bizi.com.demo.contaBancaria.ContaBancariaModel;
import bizi.com.demo.transacao.TransacaoModel;

@ExtendWith(MockitoExtension.class)
class SaqueControllerTest {

    @Mock
    private SaqueService saqueService;

    @InjectMocks
    private SaqueController controller;

    @Test
    @DisplayName("efetuarSaque: deve retornar 200 quando saque realizado")
    void efetuarSaque_deveRetornar200QuandoSucesso() {
        ContaBancariaModel conta = new ContaBancariaModel();
        conta.setSaldo(new BigDecimal("900.00"));

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);

        when(saqueService.realizarSaque(new BigDecimal("100.00"))).thenReturn(transacao);

        ResponseEntity<?> response = controller.efetuarSaque(Map.of("valor", new BigDecimal("100.00")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Saque realizado! Saldo atual: R$ 900.00");
    }

    @Test
    @DisplayName("efetuarSaque: deve retornar 400 quando service lança exceção")
    void efetuarSaque_deveRetornar400QuandoErro() {
        when(saqueService.realizarSaque(new BigDecimal("1000.00")))
                .thenThrow(new RuntimeException("Saldo insuficiente para realizar o saque."));

        ResponseEntity<?> response = controller.efetuarSaque(Map.of("valor", new BigDecimal("1000.00")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Saldo insuficiente para realizar o saque.");
    }

    @Test
    @DisplayName("saqueAdministrativo: deve retornar 200 quando saque administrativo realizado")
    void saqueAdministrativo_deveRetornar200QuandoSucesso() {
        ContaBancariaModel conta = new ContaBancariaModel();
        conta.setSaldo(new BigDecimal("500.00"));

        TransacaoModel transacao = new TransacaoModel();
        transacao.setContaBancaria(conta);

        when(saqueService.realizarSaqueAdministrativo(10L, new BigDecimal("200.00")))
                .thenReturn(transacao);

        ResponseEntity<?> response = controller.saqueAdministrativo(
                10L,
                Map.of("valor", new BigDecimal("200.00")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Saque administrativo na conta 10 realizado! Saldo: R$ 500.00");
    }

    @Test
    @DisplayName("saqueAdministrativo: deve retornar 400 quando service lança exceção")
    void saqueAdministrativo_deveRetornar400QuandoErro() {
        when(saqueService.realizarSaqueAdministrativo(99L, new BigDecimal("100.00")))
                .thenThrow(new RuntimeException("Operação Administrativa Negada: Saldo insuficiente."));

        ResponseEntity<?> response = controller.saqueAdministrativo(
                99L,
                Map.of("valor", new BigDecimal("100.00")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Operação Administrativa Negada: Saldo insuficiente.");
    }
}