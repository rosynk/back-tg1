package bizi.com.demo.config;

import bizi.com.demo.usuario.Role;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;
import bizi.com.demo.endereco.EnderecoModel;
import bizi.com.demo.endereco.EnderecoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UsuarioRepository usuarioRepository, 
                          EnderecoRepository enderecoRepository, 
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            if (!usuarioRepository.existsByCpf("00000000000")) {
                
                // 1. Criando o endereço com os nomes exatos da sua EnderecoModel
                EnderecoModel enderecoAdmin = new EnderecoModel();
                enderecoAdmin.setRua("Rua Administrativa");
                enderecoAdmin.setNumero(0); // Note: Sem aspas, pois é Integer
                enderecoAdmin.setBairro("Centro");
                enderecoAdmin.setCidade("Sede Bizi");
                enderecoAdmin.setEstado("SP");
                enderecoAdmin.setCep("00000000");
                
                // Salva o endereço primeiro
                EnderecoModel enderecoSalvo = enderecoRepository.save(enderecoAdmin);

                // 2. Criando o Usuário Admin vinculado ao endereço
                UsuarioModel admin = new UsuarioModel();
                admin.setNomeCompleto("Administrador Bizi");
                admin.setCpf("00000000000");
                admin.setEmail("admin@bizi.com");
                admin.setTelefone("11999999999");
                admin.setSenha(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setDataCadastro(LocalDateTime.now());
                admin.setEndereco(enderecoSalvo); 
                
                usuarioRepository.save(admin);
                
                System.out.println("--------------------------------------");
                System.out.println("✅ ADMIN CRIADO COM SUCESSO NO PORTA 8086!");
                System.out.println("Login: 00000000000 | Senha: admin123");
                System.out.println("--------------------------------------");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar banco: " + e.getMessage());
            e.printStackTrace();
        }
    }
}