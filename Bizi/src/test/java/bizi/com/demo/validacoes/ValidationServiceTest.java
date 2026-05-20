package bizi.com.demo.validacoes;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bizi.com.demo.usuario.UsuarioDto;
import bizi.com.demo.validacoes.CPF.CPFValidador;
import bizi.com.demo.validacoes.email.EmailValidator;
import bizi.com.demo.validacoes.telefone.TelefoneValidator;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private CPFValidador cpfValidator;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private TelefoneValidator telefoneValidator;

    @InjectMocks
    private ValidationService service;

    private UsuarioDto dto;

    @BeforeEach
    void setUp() {
        dto = new UsuarioDto();
        dto.setCpf("12345678900");
        dto.setEmail("usuario@teste.com");
        dto.setTelefone("11999999999");
    }

    @Test
    @DisplayName("validarUsuario: não deve lançar exceção quando CPF, email e telefone são válidos")
    void validarUsuario_naoDeveLancarQuandoDadosValidos() {
        when(cpfValidator.isValid("12345678900")).thenReturn(true);
        when(emailValidator.isValid("usuario@teste.com")).thenReturn(true);
        when(telefoneValidator.isValid("11999999999")).thenReturn(true);

        assertThatCode(() -> service.validarUsuario(dto))
                .doesNotThrowAnyException();

        verify(cpfValidator).isValid("12345678900");
        verify(emailValidator).isValid("usuario@teste.com");
        verify(telefoneValidator).isValid("11999999999");
    }

    @Test
    @DisplayName("validarUsuario: deve lançar exceção quando CPF é inválido")
    void validarUsuario_deveLancarQuandoCpfInvalido() {
        when(cpfValidator.isValid("12345678900")).thenReturn(false);

        assertThatThrownBy(() -> service.validarUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPF inválido");

        verify(cpfValidator).isValid("12345678900");
        verify(emailValidator, never()).isValid("usuario@teste.com");
        verify(telefoneValidator, never()).isValid("11999999999");
    }

    @Test
    @DisplayName("validarUsuario: deve lançar exceção quando email é inválido")
    void validarUsuario_deveLancarQuandoEmailInvalido() {
        when(cpfValidator.isValid("12345678900")).thenReturn(true);
        when(emailValidator.isValid("usuario@teste.com")).thenReturn(false);

        assertThatThrownBy(() -> service.validarUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email inválido");

        verify(cpfValidator).isValid("12345678900");
        verify(emailValidator).isValid("usuario@teste.com");
        verify(telefoneValidator, never()).isValid("11999999999");
    }

    @Test
    @DisplayName("validarUsuario: deve lançar exceção quando telefone é inválido")
    void validarUsuario_deveLancarQuandoTelefoneInvalido() {
        when(cpfValidator.isValid("12345678900")).thenReturn(true);
        when(emailValidator.isValid("usuario@teste.com")).thenReturn(true);
        when(telefoneValidator.isValid("11999999999")).thenReturn(false);

        assertThatThrownBy(() -> service.validarUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Telefone inválido");

        verify(cpfValidator).isValid("12345678900");
        verify(emailValidator).isValid("usuario@teste.com");
        verify(telefoneValidator).isValid("11999999999");
    }
}