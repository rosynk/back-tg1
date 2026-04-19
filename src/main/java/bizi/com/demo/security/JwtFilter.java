package bizi.com.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Imports do seu projeto
import bizi.com.demo.login.TokenService;
import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    public JwtFilter(TokenService tokenService, UsuarioRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = recoverToken(request);

        if (token != null) {
            try {
                // CORREÇÃO: Usando getSubject que é o método que existe no seu TokenService
                String email = tokenService.getSubject(token); 
                
                Optional<UsuarioModel> userOptional = repository.findByEmail(email);

                if (userOptional.isPresent()) {
                    UsuarioModel user = userOptional.get();
                    
                    // Pegamos a Role (Ex: ROLE_ADMIN) e transformamos em autoridade
                    String roleName = user.getRole().name();
                    
                    // Garantindo que tenha o prefixo ROLE_ para o Spring Security não barrar
                    if (!roleName.startsWith("ROLE_")) {
                        roleName = "ROLE_" + roleName;
                    }

                    var authority = new SimpleGrantedAuthority(roleName);
                    
                    var authentication = new UsernamePasswordAuthenticationToken(
                        user, 
                        null, 
                        List.of(authority)
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (RuntimeException e) {
                // Se o token for inválido, apenas não autentica (o Spring cuidará do 403 depois)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.replace("Bearer ", "").trim();
    }
}