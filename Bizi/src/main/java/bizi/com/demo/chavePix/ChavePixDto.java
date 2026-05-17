package bizi.com.demo.chavePix;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChavePixDto(
	    @NotNull TipoChave tipo
	) {}