package bizi.com.demo.chavePix;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Certifique-se de que o nome do Enum é TipoChave
public record ChavePixDto(
                String valor, // Se mudamos na Model para 'valor', é bom manter o padrão aqui
                @NotNull TipoChave tipo // Deve ser o mesmo nome do seu Enum
) {
}