package bizi.com.demo.contaBancaria;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Conta Bancária", description = "Endpoints para gerenciamento de contas bancárias")
public class ContaBancariaController {

    @Autowired
    private ContaBancariaService contaBancariaService;

    @PostMapping
    @Operation(summary = "Criar conta bancária", description = "Cadastra uma nova conta. Geralmente invocado pelo processo de onboarding.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado")
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
    @Operation(summary = "Listar minhas contas", description = "Retorna as contas vinculadas ao usuário logado via Token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contas listadas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<ContaBancariaModel>> buscarMinhasContas() {
        return ResponseEntity.ok(contaBancariaService.buscarMinhasContas());
    }

    @PatchMapping("/meu-deposito")
    @Operation(summary = "Auto-Depósito (Cliente)", description = "Injeta saldo na própria conta sem necessidade de informar ID na URL.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Valor inválido"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada para o usuário logado")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @Operation(summary = "Buscar conta por ID", description = "Retorna detalhes de uma conta. Clientes só podem ver a própria conta.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conta encontrada"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contaBancariaService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Conta não encontrada."));
        }
    }

    // ==========================================
    // ENDPOINTS EXCLUSIVOS PARA ADMINISTRADORES
    // ==========================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas as contas (ADMIN)", description = "Visualização global de todas as contas do banco. Requer ROLE_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    public ResponseEntity<List<ContaBancariaModel>> listarTodas() {
        return ResponseEntity.ok(contaBancariaService.listarTodas());
    }

    @PatchMapping("/{id}/deposito-administrativo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Depósito via Admin", description = "Permite que o administrador injete saldo em qualquer conta pelo ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Crédito realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Valor inválido"),
        @ApiResponse(responseCode = "404", description = "Conta destino não encontrada")
    })
    public ResponseEntity<?> depositoAdmin(@PathVariable Long id, @RequestBody BigDecimal valor) {
        try {
            contaBancariaService.depositar(id, valor);
            return ResponseEntity.ok(new ErrorResponse("Depósito administrativo de R$ " + valor + " realizado."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar conta (ADMIN)", description = "Remove uma conta permanentemente do sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada para exclusão")
    })
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
        public ErrorResponse(String mensagem) { this.mensagem = mensagem; }
        public String getMensagem() { return mensagem; }
    }
}