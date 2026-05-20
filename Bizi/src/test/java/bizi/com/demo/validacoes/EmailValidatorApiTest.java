package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.validacoes.email.EmailValidatorApi;
import bizi.com.demo.validacoes.external.EmailApiClient;

@ExtendWith(MockitoExtension.class)
class EmailValidatorApiTest {

    @Mock
    private EmailApiClient client;

    @InjectMocks
    private EmailValidatorApi validator;

    @Test
    @DisplayName("isValid: deve retornar true quando API valida email")
    void isValid_deveRetornarTrueQuandoApiValidaEmail() {
        when(client.validarEmail("usuario@teste.com")).thenReturn(true);

        boolean resultado = validator.isValid("usuario@teste.com");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false quando API invalida email")
    void isValid_deveRetornarFalseQuandoApiInvalidaEmail() {
        when(client.validarEmail("invalido")).thenReturn(false);

        boolean resultado = validator.isValid("invalido");

        assertThat(resultado).isFalse();
    }
}