package bizi.com.demo.telefone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.validacoes.external.TelefoneApiClient;
import bizi.com.demo.validacoes.telefone.TelefoneValidatorApi;

@ExtendWith(MockitoExtension.class)
class TelefoneValidatorApiTest {

    @Mock
    private TelefoneApiClient client;

    @InjectMocks
    private TelefoneValidatorApi validator;

    @Test
    @DisplayName("isValid: deve retornar true quando API valida telefone")
    void isValid_deveRetornarTrueQuandoApiValidaTelefone() {
        when(client.validarTelefone("11999999999")).thenReturn(true);

        boolean resultado = validator.isValid("11999999999");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false quando API invalida telefone")
    void isValid_deveRetornarFalseQuandoApiInvalidaTelefone() {
        when(client.validarTelefone("11000000000")).thenReturn(false);

        boolean resultado = validator.isValid("11000000000");

        assertThat(resultado).isFalse();
    }
}