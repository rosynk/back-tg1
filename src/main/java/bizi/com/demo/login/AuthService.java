package bizi.com.demo.login;



import bizi.com.demo.usuario.UsuarioModel;
import bizi.com.demo.usuario.UsuarioRepository;
import bizi.com.demo.comunicacao.ComunicacaoService;
import bizi.com.demo.login.LoginDto; // Garanta que o import está correto
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
    private TokenService tokenService; // O serviço que você criou para gerar o JWT

    /**
     * MÉTODO QUE ESTAVA FALTANDO: Faz o login e devolve o Token
     */
    public String autenticar(LoginDto dto) {
        // Note que agora usamos .email() e .senha() em vez de .getEmail() e .getSenha()
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        return tokenService.gerarToken((UsuarioModel) auth.getPrincipal());
    }

    /**
     * Solicitação de código (Esqueci a Senha)
     */
    @Transactional
    public void solicitarCodigoRecuperacao(String email) {
        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado."));

        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoRecuperacao(codigo);
        usuario.setDataExpiracaoCodigo(LocalDateTime.now().plusMinutes(15));
        
        usuarioRepository.save(usuario);
        comunicacaoService.enviarEmailRecuperacao(email, codigo);
    }

    /**
     * Redefinição real da senha
     */
    @Transactional
    public void redefinirSenha(String email, String codigo, String novaSenha) {
        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado."));

        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(codigo)) {
            throw new RuntimeException("Código inválido.");
        }

        if (usuario.getDataExpiracaoCodigo().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirou.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setCodigoRecuperacao(null);
        usuario.setDataExpiracaoCodigo(null);
        usuarioRepository.save(usuario);
    }
}