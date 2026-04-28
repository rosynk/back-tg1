package bizi.com.demo.extrato;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/extrato")
@Tag(name = "Extrato", description = "Gerenciamento de histórico financeiro")
public class ExtratoController {

    @Autowired
    private ExtratoService extratoService;

    @Operation(summary = "Obter extrato detalhado (Cliente)", description = "Retorna o extrato da conta do usuário logado através do Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar extrato")
    })
    @CrossOrigin(origins = "http://localhost:4200")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> obterExtrato() {
        try {
            return ResponseEntity.ok(extratoService.gerarExtrato());
        } catch (Exception e) {
            // Imprime o erro real no console do Java para depuração
            System.err.println("❌ Erro ao gerar extrato: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar extrato: " + e.getMessage());
        }
    }

    @Operation(summary = "[ADM] Consultar extrato de qualquer conta", description = "Permite que administradores visualizem a movimentação de qualquer conta pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Somente administradores"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @GetMapping("/admin/{idConta}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> obterExtratoPorId(@PathVariable Long idConta) {
        try {
            return ResponseEntity.ok(extratoService.gerarExtratoParaAdm(idConta));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}