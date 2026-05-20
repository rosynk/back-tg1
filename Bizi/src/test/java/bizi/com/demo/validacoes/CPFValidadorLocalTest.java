package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bizi.com.demo.validacoes.CPF.CPFValidadorLocal;

class CPFValidadorLocalTest {

    private final CPFValidadorLocal validator = new CPFValidadorLocal();

    @Test
    @DisplayName("isValid: deve retornar true para CPF válido")
    void isValid_deveRetornarTrueParaCpfValido() {
        boolean resultado = validator.isValid("52998224725");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false para CPF null")
    void isValid_deveRetornarFalseParaCpfNull() {
        boolean resultado = validator.isValid(null);

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para CPF com menos de 11 dígitos")
    void isValid_deveRetornarFalseParaCpfComMenosDeOnzeDigitos() {
        boolean resultado = validator.isValid("1234567890");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para CPF com mais de 11 dígitos")
    void isValid_deveRetornarFalseParaCpfComMaisDeOnzeDigitos() {
        boolean resultado = validator.isValid("123456789000");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para CPF com letras")
    void isValid_deveRetornarFalseParaCpfComLetras() {
        boolean resultado = validator.isValid("1234567890A");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para CPF com todos os números iguais")
    void isValid_deveRetornarFalseParaCpfComNumerosIguais() {
        boolean resultado = validator.isValid("11111111111");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false quando primeiro dígito verificador é inválido")
    void isValid_deveRetornarFalseQuandoPrimeiroDigitoInvalido() {
        boolean resultado = validator.isValid("52998224715");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false quando segundo dígito verificador é inválido")
    void isValid_deveRetornarFalseQuandoSegundoDigitoInvalido() {
        boolean resultado = validator.isValid("52998224724");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar true para outro CPF válido")
    void isValid_deveRetornarTrueParaOutroCpfValido() {
        boolean resultado = validator.isValid("11144477735");

        assertThat(resultado).isTrue();
    }
}