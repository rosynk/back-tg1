package bizi.com.demo.login;

import bizi.com.demo.usuario.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Recursos de segurança do BiziBanco")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Login padrão", description = "Autentica o usuário e retorna um token JWT com a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "CPF ou senha incorretos"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        String token = authService.autenticar(loginDto);
        String role = usuarioRepository.findByCpf(loginDto.cpf())
                .map(u -> u.getRole().name())
                .orElse("ROLE_CLIENTE");
        return ResponseEntity.ok(new TokenResponseDto(token, role));
    }

    @Operation(summary = "Gera código de recuperação e envia via e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
            @ApiResponse(responseCode = "404", description = "E-mail não encontrado")
    })
    @PostMapping("/recuperar-senha")
    public ResponseEntity<String> solicitarCodigo(@RequestBody @Valid RecuperacaoRequestDto dto) {
        authService.solicitarCodigoRecuperacao(dto.email());
        return ResponseEntity.ok("Código enviado para o e-mail cadastrado.");
    }

    @Operation(summary = "Define nova senha com validação de código")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou expirado")
    })
    @PostMapping("/redefinir-senha")
    public ResponseEntity<String> redefinirSenha(@RequestBody @Valid RedefinirSenhaDto dto) {
        authService.redefinirSenha(dto.email(), dto.codigo(), dto.novaSenha());
        return ResponseEntity.ok("Senha atualizada com sucesso.");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleDisabled(DisabledException e) {
        return ResponseEntity.status(403).body(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(403).body("CPF ou senha incorretos.");
    }
}