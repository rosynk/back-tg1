package bizi.com.demo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import bizi.com.demo.security.JwtFilter;

class SecurityConfigTest {

    @Test
    @DisplayName("passwordEncoder: deve retornar BCryptPasswordEncoder")
    void passwordEncoder_deveRetornarBCryptPasswordEncoder() {
        JwtFilter jwtFilter = Mockito.mock(JwtFilter.class);
        SecurityConfig config = new SecurityConfig(jwtFilter);

        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        assertThat(encoder.encode("senha")).isNotBlank();
    }

    @Test
    @DisplayName("authenticationManager: deve delegar para AuthenticationConfiguration")
    void authenticationManager_deveDelegarParaAuthenticationConfiguration() throws Exception {
        JwtFilter jwtFilter = Mockito.mock(JwtFilter.class);
        SecurityConfig config = new SecurityConfig(jwtFilter);

        AuthenticationConfiguration authenticationConfiguration = Mockito.mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);

        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager resultado = config.authenticationManager(authenticationConfiguration);

        assertThat(resultado).isEqualTo(authenticationManager);
    }

    @Test
    @DisplayName("corsConfigurationSource: deve configurar CORS esperado")
    void corsConfigurationSource_deveConfigurarCorsEsperado() {
        JwtFilter jwtFilter = Mockito.mock(JwtFilter.class);
        SecurityConfig config = new SecurityConfig(jwtFilter);

        CorsConfigurationSource source = config.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setRequestURI("/api/teste");

        CorsConfiguration cors = source.getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins())
                .containsExactly("http://localhost:4200", "http://127.0.0.1:4200");
        assertThat(cors.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}