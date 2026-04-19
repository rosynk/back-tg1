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
@Tag(name = "Autenticação", description = "Endpoints para login e fluxo de recuperação de senha")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Realiza o login", description = "Autentica o usuário e retorna um token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "403", description = "E-mail ou senha incorretos"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        var token = authService.autenticar(loginDto);
        return ResponseEntity.ok(new TokenResponseDto(token));
    }

    @Operation(summary = "Solicitar código", description = "Envia um código de 6 dígitos para o e-mail do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
        @ApiResponse(responseCode = "404", description = "E-mail não encontrado na base de dados")
    })
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> solicitarCodigo(@RequestBody @Valid RecuperacaoRequestDto dto) {
        authService.solicitarCodigoRecuperacao(dto.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Redefinir senha", description = "Valida o código recebido e atualiza para a nova senha")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Código inválido ou expirado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid RedefinirSenhaDto dto) {
        authService.redefinirSenha(dto.email(), dto.codigo(), dto.novaSenha());
        return ResponseEntity.ok().build();
    }
}