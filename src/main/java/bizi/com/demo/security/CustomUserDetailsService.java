package bizi.com.demo.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // 🔥 O segredo está aqui: 
        // Pegamos a Role do banco. Se no banco estiver "ADMIN", 
        // a autoridade será "ROLE_ADMIN". 
        // O prefixo "ROLE_" é um padrão do Spring Security.
        String roleName = "ROLE_" + usuario.getRole().name(); 

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }
}