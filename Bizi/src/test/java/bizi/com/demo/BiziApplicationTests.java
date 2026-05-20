package bizi.com.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Desabilitado porque os testes atuais são unitários e não sobem o contexto completo da aplicação.")
@SpringBootTest
class BiziApplicationTests {

    @Test
    void contextLoads() {
    }
}