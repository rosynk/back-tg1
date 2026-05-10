package bizi.com.demo.security;

import bizi.com.demo.login.TokenService;
import bizi.com.demo.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

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

        // LOG DE ENTRADA: Verifica se o header chegou
        if (request.getRequestURI().startsWith("/api")) {
            System.out.println("🔍 [Filtro] Requisição para: " + request.getRequestURI() + " | Token presente: "
                    + (token != null));
        }

        if (token != null) {
            try {
                String cpf = tokenService.getClaimCpf(token);

                if (cpf != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var usuarioOpt = repository.findByCpf(cpf);

                    if (usuarioOpt.isPresent()) {
                        var user = usuarioOpt.get();
                        String roleName = user.getRole().name();
                        // Garante o prefixo ROLE_ para o Spring Security
                        String finalRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

                        var authority = new SimpleGrantedAuthority(finalRole);
                        var authentication = new UsernamePasswordAuthenticationToken(user, null,
                                Collections.singletonList(authority));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✅ [Auth] Sucesso! CPF: " + cpf + " acessando com " + finalRole);
                    } else {
                        System.out.println("⚠️ [Auth] CPF " + cpf + " não encontrado no banco.");
                    }
                }
            } catch (Exception e) {
                // Se o token expirou ou a assinatura é inválida, cairá aqui
                System.err.println("❌ [Auth] Erro ao validar token JWT: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return null;
        return authHeader.replace("Bearer ", "").trim();
    }
}