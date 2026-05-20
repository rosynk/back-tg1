package bizi.com.demo.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private ResourceHandlerRegistry registry;

    @Mock
    private ResourceHandlerRegistration registration;

    @InjectMocks
    private WebConfig config;

    @Test
    @DisplayName("addResourceHandlers: deve registrar handler para uploads com barra final")
    void addResourceHandlers_deveRegistrarHandlerComBarraFinal() throws Exception {
        setRaizLocal("C:\\uploads\\");

        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);

        config.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations("file:C:/uploads/");
    }

    @Test
    @DisplayName("addResourceHandlers: deve registrar handler para uploads sem barra final")
    void addResourceHandlers_deveRegistrarHandlerSemBarraFinal() throws Exception {
        setRaizLocal("C:\\uploads");

        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);

        config.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations("file:C:/uploads/");
    }

    private void setRaizLocal(String valor) throws Exception {
        Field field = WebConfig.class.getDeclaredField("raizLocal");
        field.setAccessible(true);
        field.set(config, valor);
    }
}