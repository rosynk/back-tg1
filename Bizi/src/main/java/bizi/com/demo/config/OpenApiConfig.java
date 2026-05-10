package bizi.com.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Bizi API", 
        version = "1.0",
        description = "Documentação da API Bizi com autenticação JWT"
    ),
    // Isso aplica o ícone do cadeado globalmente no Swagger UI
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth", // O nome aqui deve ser EXATAMENTE igual ao usado no security acima
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Cole aqui seu token JWT obtido no endpoint de login. Exemplo: 'eyJhbG...'"
)
public class OpenApiConfig {
    // Não é necessário código adicional aqui, as anotações fazem o trabalho.
}