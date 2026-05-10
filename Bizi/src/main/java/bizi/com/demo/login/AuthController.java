package bizi.com.demo.login;

import org.springframework.http.ResponseEntity;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login padrão", description = "Autentica o usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "CPF ou senha incorretos"), // Atualizado para CPF
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        String token = authService.autenticar(loginDto);
        return ResponseEntity.ok(new TokenResponseDto(token));
    }

    @Operation(summary = "Login específico para a plataforma do cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "CPF ou senha incorretos"), // Atualizado para CPF
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    @PostMapping("/loginCliente")
    public ResponseEntity<TokenResponseDto> loginCliente(@RequestBody @Valid LoginDto loginDto) {
        // O @RequestBody é essencial para converter o JSON do Angular no seu Record
        // LoginDto
        String token = authService.autenticar(loginDto);
        return ResponseEntity.ok(new TokenResponseDto(token));
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
}