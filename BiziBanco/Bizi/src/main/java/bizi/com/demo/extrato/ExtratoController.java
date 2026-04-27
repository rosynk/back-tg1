package bizi.com.demo.extrato;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Obter extrato detalhado (Cliente)", 
               description = "Retorna o extrato da conta do usuário logado através do Token.")
    @GetMapping
    public ResponseEntity<ExtratoResponseDto> obterExtrato() {
        return ResponseEntity.ok(extratoService.gerarExtrato());
    }

    // --- NOVO ENDPOINT PARA O ADM ---
    @Operation(summary = "[ADM] Consultar extrato de qualquer conta", 
               description = "Permite que administradores visualizem a movimentação de qualquer conta pelo ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado: Somente administradores"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @GetMapping("/admin/{idConta}")
    public ResponseEntity<ExtratoResponseDto> obterExtratoPorId(@PathVariable Long idConta) {
        return ResponseEntity.ok(extratoService.gerarExtratoParaAdm(idConta));
    }
}