package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bizi.com.demo.validacoes.external.TelefoneResponse;

class TelefoneResponseTest {

    @Test
    @DisplayName("isValid: deve retornar valor do campo valid")
    void isValid_deveRetornarValorDoCampoValid() throws Exception {
        TelefoneResponse response = new TelefoneResponse();

        Field field = TelefoneResponse.class.getDeclaredField("valid");
        field.setAccessible(true);
        field.set(response, true);

        assertThat(response.isValid()).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false por padrão")
    void isValid_deveRetornarFalsePorPadrao() {
        TelefoneResponse response = new TelefoneResponse();

        assertThat(response.isValid()).isFalse();
    }
}