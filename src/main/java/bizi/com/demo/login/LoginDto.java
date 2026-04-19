package bizi.com.demo.login;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    String senha
) {}