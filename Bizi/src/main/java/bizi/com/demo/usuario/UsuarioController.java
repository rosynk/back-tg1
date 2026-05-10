package bizi.com.demo.usuario;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuário", description = "Endpoints para gerenciamento interno de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Criar usuário
     * Nota: Para novos clientes deslogados, usar o PropostaController.
     */
    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cadastra um novo usuário. Se feito por um Admin, permite definir Roles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou duplicados"),
        @ApiResponse(responseCode = "403", description = "Sem permissão (Hierarquia de Roles)"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> criarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioModel usuario = usuarioService.criarUsuario(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    /**
     * Busca um usuário pelo CPF
     */
    @GetMapping("/cpf/{cpf}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'FILHO')")
    @Operation(summary = "Buscar por CPF", description = "Retorna os dados do usuário pelo CPF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<UsuarioModel> buscarPorCpf(
            @Parameter(description = "CPF do usuário (apenas números)")
            @PathVariable String cpf) {
        return ResponseEntity.ok(usuarioService.buscarPorCpf(cpf));
    }

    /**
     * Busca um usuário pelo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'FILHO')")
    @Operation(summary = "Buscar por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioModel> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * Lista todos os usuários
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos", description = "Retorna todos os usuários (Acesso restrito ao Administrador)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado: Requer ROLE_ADMIN")
    })
    public ResponseEntity<List<UsuarioModel>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Atualiza um usuário
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados. Admin pode tudo; Cliente só o próprio perfil.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
        @ApiResponse(responseCode = "400", description = "Erro na atualização"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            return ResponseEntity.ok(usuarioService.atualizarUsuario(id, usuarioDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    /**
     * Deleta um usuário pelo ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Somente ADMIN pode deletar")
    })
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deleta um usuário pelo CPF
     */
    @DeleteMapping("/cpf/{cpf}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar por CPF")
    public ResponseEntity<Void> deletarUsuarioPorCpf(@PathVariable String cpf) {
        usuarioService.deletarUsuarioPorCpf(cpf);
        return ResponseEntity.noContent().build();
    }

    // Classe auxiliar para manter as respostas de erro padronizadas para o Front
    public static class ErrorMessage {
        private String message;
        public ErrorMessage(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}