package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.validacoes.CPF.CpfValidatorApi;
import bizi.com.demo.validacoes.external.CpfApiClient;

@ExtendWith(MockitoExtension.class)
class CpfValidatorApiTest {

    @Mock
    private CpfApiClient client;

    @InjectMocks
    private CpfValidatorApi validator;

    @Test
    @DisplayName("isValid: deve retornar true quando API valida CPF")
    void isValid_deveRetornarTrueQuandoApiValidaCpf() {
        when(client.validarCpf("52998224725")).thenReturn(true);

        boolean resultado = validator.isValid("52998224725");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false quando API invalida CPF")
    void isValid_deveRetornarFalseQuandoApiInvalidaCpf() {
        when(client.validarCpf("12345678900")).thenReturn(false);

        boolean resultado = validator.isValid("12345678900");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false quando API lança exceção")
    void isValid_deveRetornarFalseQuandoApiLancaExcecao() {
        when(client.validarCpf("52998224725")).thenThrow(new RuntimeException("API fora do ar"));

        boolean resultado = validator.isValid("52998224725");

        assertThat(resultado).isFalse();
    }
}