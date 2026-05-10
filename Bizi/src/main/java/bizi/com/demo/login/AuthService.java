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
        // 1. Tenta autenticar no Spring Security
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.cpf(), dto.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // 2. ✅ CORREÇÃO DO ERRO 500:
        // Em vez de fazer Cast do Principal, buscamos o UsuarioModel direto do banco
        // pelo CPF
        // Isso garante que o TokenService receba o objeto correto (UsuarioModel)
        UsuarioModel usuario = usuarioRepository.findByCpf(dto.cpf())
                .orElseThrow(() -> new RuntimeException("Erro ao carregar dados do usuário após autenticação."));

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