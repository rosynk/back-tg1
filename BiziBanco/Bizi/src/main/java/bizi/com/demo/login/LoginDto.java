package bizi.com.demo.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginDto(
                @NotBlank(message = "O CPF é obrigatório") String cpf,

                @NotBlank(message = "A senha é obrigatória") String senha) {
}