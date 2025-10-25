package bizi.com.demo.usuario;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuário", description = "Endpoints para gerenciamento de usuários")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Cria um novo usuário
     */
    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cadastra um novo usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/Email já cadastrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UsuarioModel> criarUsuario(
            @Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioModel usuario = usuarioService.criarUsuario(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (UsuarioConflictException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca um usuário pelo CPF
     */
    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar usuário por CPF", description = "Retorna os dados do usuário pelo CPF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "CPF inválido")
    })
    public ResponseEntity<UsuarioModel> buscarPorCpf(
            @Parameter(description = "CPF do usuário (apenas números)")
            @PathVariable String cpf) {
        try {
            UsuarioModel usuario = usuarioService.buscarPorCpf(cpf);
            return ResponseEntity.ok(usuario);
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca um usuário pelo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados do usuário pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioModel> buscarPorId(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {
        try {
            UsuarioModel usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todos os usuários
     */
    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna uma lista com todos os usuários cadastrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    })
    public ResponseEntity<List<UsuarioModel>> listarTodos() {
        List<UsuarioModel> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Atualiza um usuário
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/Email já cadastrado")
    })
    public ResponseEntity<UsuarioModel> atualizarUsuario(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioModel usuario = usuarioService.atualizarUsuario(id, usuarioDto);
            return ResponseEntity.ok(usuario);
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UsuarioConflictException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deleta um usuário pelo ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário por ID", description = "Remove um usuário do sistema pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deletarUsuario(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {
        try {
            usuarioService.deletarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deleta um usuário pelo CPF
     */
    @DeleteMapping("/cpf/{cpf}")
    @Operation(summary = "Deletar usuário por CPF", description = "Remove um usuário do sistema pelo CPF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deletarUsuarioPorCpf(
            @Parameter(description = "CPF do usuário (apenas números)")
            @PathVariable String cpf) {
        try {
            usuarioService.deletarUsuarioPorCpf(cpf);
            return ResponseEntity.noContent().build();
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
