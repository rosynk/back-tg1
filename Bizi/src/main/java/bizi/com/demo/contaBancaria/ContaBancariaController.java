package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Conta Bancária", description = "Endpoints para gerenciamento de contas bancárias e operações de saldo")
@CrossOrigin(origins = "http://localhost:4200") // ✅ Libera a comunicação com o Angular na porta 8086
public class ContaBancariaController {

    @Autowired
    private ContaBancariaService contaBancariaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENTE')")
    @Operation(summary = "Listar contas (Híbrido)", description = "Retorna todas as contas se for ADMIN, ou apenas as contas do usuário logado se for CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso na recuperação dos dados"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão de acesso")
    })
    public ResponseEntity<?> listarContas() {
        // Verifica a autoridade real no SecurityContext
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(contaBancariaService.listarTodas());
        } else {
            return ResponseEntity.ok(contaBancariaService.buscarMinhasContas());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENTE')")
    @Operation(summary = "Criar conta bancária", description = "Cadastra uma nova conta vinculada a um usuário existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<?> criarConta(@Valid @RequestBody ContaBancariaDto contaBancariaDto) {
        try {
            ContaBancariaModel conta = contaBancariaService.criarConta(contaBancariaDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/minhas-contas")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    @Operation(summary = "Minhas Contas", description = "Endpoint específico para o cliente listar suas próprias contas.")
    @ApiResponse(responseCode = "200", description = "Lista de contas do cliente retornada")
    public ResponseEntity<List<ContaBancariaModel>> buscarMinhasContas() {
        return ResponseEntity.ok(contaBancariaService.buscarMinhasContas());
    }

    @PatchMapping("/meu-deposito")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    @Operation(summary = "Auto-Depósito", description = "Permite ao cliente depositar em sua própria conta principal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito efetuado"),
            @ApiResponse(responseCode = "400", description = "Valor negativo ou inválido")
    })
    public ResponseEntity<?> autoDepositoSemId(@RequestBody BigDecimal valor) {
        try {
            contaBancariaService.realizarAutoDepositoLogado(valor);
            return ResponseEntity.ok(new ErrorResponse("Depósito de R$ " + valor + " realizado com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENTE')")
    @Operation(summary = "Buscar por ID", description = "Retorna detalhes de uma conta específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta localizada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contaBancariaService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Conta inexistente ou acesso negado."));
        }
    }

    // ==========================================
    // ÁREA ADMINISTRATIVA
    // ==========================================

    @PatchMapping("/{id}/deposito-administrativo")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Depósito Administrativo", description = "Ação exclusiva do ADMIN para injetar saldo em qualquer conta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo atualizado pelo administrador"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Requer ROLE_ADMIN")
    })
    public ResponseEntity<?> depositoAdmin(@PathVariable Long id, @RequestBody BigDecimal valor) {
        try {
            contaBancariaService.depositar(id, valor);
            return ResponseEntity.ok(new ErrorResponse("Crédito administrativo processado com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Remover Conta", description = "Exclui permanentemente uma conta bancária. Requer ROLE_ADMIN.")
    @ApiResponse(responseCode = "204", description = "Conta removida com sucesso")
    public ResponseEntity<Void> deletarConta(@PathVariable Long id) {
        try {
            contaBancariaService.deletarConta(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    static class ErrorResponse {
        private String mensagem;

        public ErrorResponse(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}