package bizi.com.demo.login;

import bizi.com.demo.usuario.UsuarioModel;

import bizi.com.demo.usuario.UsuarioRepository;
import bizi.com.demo.comunicacao.ComunicacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ComunicacaoService comunicacaoService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    /**
     * Realiza a autenticação via Spring Security usando CPF e gera o Token JWT
     */
    public String autenticar(LoginDto dto) {
        // Busca o usuário antes de autenticar para checar a role
        UsuarioModel usuario = usuarioRepository.findByCpf(dto.cpf())
                .orElseThrow(() -> new RuntimeException("CPF não encontrado."));

        // ADMINs não precisam de aprovação — autenticação direta
        if (usuario.getRole().name().equals("ROLE_ADMIN")) {
            // Força autenticação sem passar pelo isEnabled()
            if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
                throw new BadCredentialsException("CPF ou senha incorretos.");
            }
        } else {
            // Clientes passam pelo fluxo normal (checa isEnabled/ativo)
            var token = new UsernamePasswordAuthenticationToken(dto.cpf(), dto.senha());
            this.authenticationManager.authenticate(token);
        }

        return tokenService.gerarToken(usuario);
    }
    /**
     * Recuperação por e-mail
     */
    @Transactional
    public void solicitarCodigoRecuperacao(String email) {
        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado na base de dados."));

        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoRecuperacao(codigo);
        usuario.setDataExpiracaoCodigo(LocalDateTime.now().plusMinutes(15));

        usuarioRepository.save(usuario);
        comunicacaoService.enviarEmailRecuperacao(email, codigo);
    }

    @Transactional
    public void redefinirSenha(String email, String codigo, String novaSenha) {
        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(codigo)) {
            throw new RuntimeException("Código de validação incorreto.");
        }

        if (usuario.getDataExpiracaoCodigo().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Este código de recuperação já expirou.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setCodigoRecuperacao(null);
        usuario.setDataExpiracaoCodigo(null);
        usuarioRepository.save(usuario);
    }
}