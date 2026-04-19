package bizi.com.demo.login;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import bizi.com.demo.usuario.UsuarioModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret:minha-senha-secreta-123}")
    private String secret;

    public String gerarToken(UsuarioModel usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            
            // 🔥 AQUI ESTÁ A LÓGICA:
            // Pegamos o nome da role (ex: "ADMIN") e garantimos que vire "ROLE_ADMIN"
            String roleComPrefixo = usuario.getRole().name();
            if (!roleComPrefixo.startsWith("ROLE_")) {
                roleComPrefixo = "ROLE_" + roleComPrefixo;
            }

            return JWT.create()
                    .withIssuer("bizi-api")
                    .withSubject(usuario.getEmail())
                    .withClaim("role", roleComPrefixo) // Agora salva com o prefixo correto
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("bizi-api")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}