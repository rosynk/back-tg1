package bizi.com.demo.telefone;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bizi.com.demo.validacoes.telefone.TelefoneValidatorLocal;

class TelefoneValidatorLocalTest {

    private final TelefoneValidatorLocal validator = new TelefoneValidatorLocal();

    @Test
    @DisplayName("isValid: deve retornar true para telefone com 11 dígitos")
    void isValid_deveRetornarTrueParaTelefoneComOnzeDigitos() {
        boolean resultado = validator.isValid("11999999999");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false para telefone null")
    void isValid_deveRetornarFalseParaTelefoneNull() {
        boolean resultado = validator.isValid(null);

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para telefone com menos de 11 dígitos")
    void isValid_deveRetornarFalseParaTelefoneComMenosDeOnzeDigitos() {
        boolean resultado = validator.isValid("1199999999");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para telefone com mais de 11 dígitos")
    void isValid_deveRetornarFalseParaTelefoneComMaisDeOnzeDigitos() {
        boolean resultado = validator.isValid("119999999999");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para telefone com letras")
    void isValid_deveRetornarFalseParaTelefoneComLetras() {
        boolean resultado = validator.isValid("1199999999A");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para telefone com máscara")
    void isValid_deveRetornarFalseParaTelefoneComMascara() {
        boolean resultado = validator.isValid("(11)99999-9999");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para string vazia")
    void isValid_deveRetornarFalseParaStringVazia() {
        boolean resultado = validator.isValid("");

        assertThat(resultado).isFalse();
    }
}