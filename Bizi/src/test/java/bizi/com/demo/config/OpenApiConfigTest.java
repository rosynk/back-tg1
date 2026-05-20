package bizi.com.demo.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

class OpenApiConfigTest {

    @Test
    @DisplayName("OpenApiConfig: deve estar anotada como Configuration")
    void deveEstarAnotadaComoConfiguration() {
        assertThat(OpenApiConfig.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("OpenApiConfig: deve possuir OpenAPIDefinition")
    void devePossuirOpenApiDefinition() {
        OpenAPIDefinition annotation = OpenApiConfig.class.getAnnotation(OpenAPIDefinition.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.info().title()).isEqualTo("Bizi API");
        assertThat(annotation.info().version()).isEqualTo("1.0");
        assertThat(annotation.security()[0].name()).isEqualTo("bearerAuth");
    }

    @Test
    @DisplayName("OpenApiConfig: deve possuir SecurityScheme bearerAuth")
    void devePossuirSecuritySchemeBearerAuth() {
        SecurityScheme annotation = OpenApiConfig.class.getAnnotation(SecurityScheme.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("bearerAuth");
        assertThat(annotation.scheme()).isEqualTo("bearer");
        assertThat(annotation.bearerFormat()).isEqualTo("JWT");
    }
}