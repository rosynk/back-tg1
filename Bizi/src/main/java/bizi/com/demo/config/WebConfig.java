package bizi.com.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${bizi.storage.raiz}")
    private String raizLocal;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve o caminho para o formato que o Java entende (file:/C:/...)
        String path = raizLocal.replace("\\", "/");
        if (!path.endsWith("/")) {
            path += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Libera o Angular (porta 4200) para acessar o Spring (8086)
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
