package bizi.com.demo.login;

import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaDto(
    @NotBlank String email,
    @NotBlank String codigo,
    @NotBlank String novaSenha
) {}