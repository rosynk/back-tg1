package bizi.com.demo.logAcesso;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bizi.com.demo.usuario.UsuarioNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/logs-acesso")
@Tag(name = "Log de Acesso", description = "Endpoints para controle de acessos dos usuários")
public class LogAcessoController {

    @Autowired
    private LogAcessoService logAcessoService;

    @PostMapping
    @Operation(summary = "Registrar log de acesso",
               description = "Registra uma nova entrada de log para um usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Log registrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> registrarLog(@Valid @RequestBody LogAcessoDto dto) {
        try {
            LogAcessoModel log = logAcessoService.registrarLog(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(log);
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao registrar log: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar log por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Log encontrado"),
        @ApiResponse(responseCode = "404", description = "Log não encontrado")
    })
    public ResponseEntity<LogAcessoModel> buscarPorId(
            @Parameter(description = "ID do log") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(logAcessoService.buscarPorId(id));
        } catch (LogAcessoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Buscar logs por usuário",
               description = "Retorna todos os logs de acesso de um usuário, do mais recente ao mais antigo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logs encontrados"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> buscarPorUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long idUsuario) {
        try {
            return ResponseEntity.ok(logAcessoService.buscarPorUsuario(idUsuario));
        } catch (UsuarioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/usuario/{idUsuario}/tipo")
    @Operation(summary = "Buscar logs por usuário e tipo de ação",
               description = "Filtra os logs de um usuário por tipo de ação (ex: LOGIN, LOGOUT)")
    public ResponseEntity<List<LogAcessoModel>> buscarPorUsuarioETipo(
            @Parameter(description = "ID do usuário") @PathVariable Long idUsuario,
            @Parameter(description = "Tipo da ação (LOGIN, LOGOUT, TRANSFERENCIA, PIX...)")
            @RequestParam String tipoAcao) {
        return ResponseEntity.ok(logAcessoService.buscarPorUsuarioETipo(idUsuario, tipoAcao));
    }

    @GetMapping("/tipo/{tipoAcao}")
    @Operation(summary = "Buscar logs por tipo de ação",
               description = "Retorna todos os logs de um determinado tipo de ação")
    public ResponseEntity<List<LogAcessoModel>> buscarPorTipoAcao(
            @Parameter(description = "Tipo da ação") @PathVariable String tipoAcao) {
        return ResponseEntity.ok(logAcessoService.buscarPorTipoAcao(tipoAcao));
    }
}