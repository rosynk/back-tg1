package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bizi.com.demo.validacoes.email.EmailValidatorLocal;

class EmailValidatorLocalTest {

    private final EmailValidatorLocal validator = new EmailValidatorLocal();

    @Test
    @DisplayName("isValid: deve retornar true para email válido simples")
    void isValid_deveRetornarTrueParaEmailValidoSimples() {
        boolean resultado = validator.isValid("usuario@teste.com");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar true para email com ponto, hífen, underline e mais")
    void isValid_deveRetornarTrueParaEmailComCaracteresPermitidos() {
        boolean resultado = validator.isValid("usuario.teste+tag-01@sub.dominio.com");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("isValid: deve retornar false para email null")
    void isValid_deveRetornarFalseParaEmailNull() {
        boolean resultado = validator.isValid(null);

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para email sem arroba")
    void isValid_deveRetornarFalseParaEmailSemArroba() {
        boolean resultado = validator.isValid("usuarioteste.com");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para email sem usuário")
    void isValid_deveRetornarFalseParaEmailSemUsuario() {
        boolean resultado = validator.isValid("@teste.com");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para email sem domínio")
    void isValid_deveRetornarFalseParaEmailSemDominio() {
        boolean resultado = validator.isValid("usuario@");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("isValid: deve retornar false para string vazia")
    void isValid_deveRetornarFalseParaStringVazia() {
        boolean resultado = validator.isValid("");

        assertThat(resultado).isFalse();
    }
}